from datetime import datetime
from kafka import KafkaProducer
from airflow.operators.python import PythonOperator
from airflow.exceptions import AirflowFailException
import os
import requests
import time
import json

##########################################################################
#
# Globals
#
##########################################################################

#
# Environment variables defined in docker-compose.yml
#
##########################################################################

# Internal Docker network communication with the Simulated HPC service
HPC_API_URL = os.getenv("HPC_API_URL")

# Internal Docker network communication with the MLOps Microservices API
MS_API_URL = os.getenv("MS_API_URL")

##########################################################################
#
# Helpers
#
##########################################################################


def send_kafka_message(message):

    try:
        json.dumps(message)
    except TypeError as e:
        raise AirflowFailException(f"Kafka message is not JSON serializable: {e}")

    producer = KafkaProducer(
        bootstrap_servers="kafka:9092",
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    )

    producer.send(  # NOTE: see application-properties of service mlops-api
        "airflow-events", message
    )

    producer.flush()


#
# DAG Tasks scoped to the Data Generation & Labeling (DataOps) phase,
# which includes the following steps:
#
# - Generate (DataOps phase; exploration/exploitation)
# - ETL model (DataOps phase; Feature Store Lite)
#
########################################################################


# Generate (DataOps phase; exploration/exploitation)
def submit_jobs(dag):

    def _submit(**kwargs):

        # NOTE: When parameters are passed to an Airflow REST API endpoint,
        #   the conf dictionary is passed via the DAG Run context and can
        #   be retrieved inside any task using the kwargs variable.
        dag_conf = kwargs["dag_run"].conf

        task_conf = dag_conf.get("explore_cells_task", {})
        nominal_composition = task_conf.get("nominal_composition")
        runs_jobs = task_conf.get("runs_jobs", [])

        all_job_ids = []
        for run in runs_jobs:

            run_number = run.get("run_number")

            previous_job_id = None

            for i, job in enumerate(run.get("jobs", [])):

                payload = {
                    "input_file": job["input_file"],
                    "output_files": job["output_files"],
                }

                # Checking dependency
                if i > 0 and previous_job_id:
                    payload["depends_on_job_id"] = previous_job_id

                response = requests.post(f"{HPC_API_URL}/api/v1/jobs", json=payload)

                if response.status_code != 200:

                    # Notifying the MLOps back-end via Kafka message
                    message = {
                        "type": "RUN_SUBMISSION_FAILED",  # TODO: the type is a Java Enum in Spring Boot gateway REST API
                        "nominal_composition": nominal_composition,
                        "run_number": run_number,
                        "external_pipeline_run_id": kwargs["dag_run"].run_id,
                        "timestamp": datetime.utcnow().isoformat() + "Z",
                    }

                    send_kafka_message(message)

                    raise AirflowFailException(
                        f"Failed to submit job. URL: {HPC_API_URL}/api/v1/jobs\n"
                        f"Payload: {payload}\n"
                        f"Status Code: {response.status_code}\n"
                        f"Response: {response.text}"
                    )

                current_job_id = response.json().get("id")
                all_job_ids.append(current_job_id)

                previous_job_id = current_job_id  # next job will depend on this one

            # Notifying the MLOps back-end via Kafka message
            message = {
                "type": "RUN_SUBMITTED",  # TODO: the type is a Java Enum in Spring Boot gateway REST API
                "nominal_composition": nominal_composition,
                "run_number": run_number,
                "external_pipeline_run_id": kwargs["dag_run"].run_id,
                "timestamp": datetime.utcnow().isoformat() + "Z",
            }

            send_kafka_message(message)

        # Will go to XCom
        return all_job_ids

    return PythonOperator(task_id="submit_jobs", python_callable=_submit, dag=dag)


# Sensor for Generate (DataOps phase; exploration/exploitation)
def wait_for_jobs(dag):

    def _wait(**kwargs):

        # NOTE: Getting access to the Task Instance object. It represents the current
        #   execution of a task inside a DAG run. It's injected automatically into PythonOperator
        #   functions that accept **kwargs.
        ti = kwargs["ti"]

        # NOTE: xcom_pull is a method on the TaskInstance object (ti) that allows one
        #   to retrieve data shared by a previous task via XCom (short for “Cross-Communication”).
        #   In the following, all_job_ids is what the task submit_jobs has returned.
        all_job_ids = ti.xcom_pull(task_ids="submit_jobs")
        if not all_job_ids:
            raise AirflowFailException("No job IDs found in XCom")

        polling_interval = 10  # seconds; TODO: should go to docker-compose.yml
        max_retries = 360  # retry for 1 hour max (360 * 10s); TODO: should go to docker-compose.yml

        for attempt in range(max_retries):

            statuses = []
            ids_messaged = []
            for job_id in all_job_ids:

                response = requests.get(f"{HPC_API_URL}/api/v1/jobs/{job_id}")

                if response.status_code != 200:
                    raise AirflowFailException(
                        f"Failed to get job status. URL: {HPC_API_URL}/api/v1/jobs/{job_id}\n"
                        f"Status Code: {response.status_code}\n"
                        f"Response: {response.text}"
                    )

                statuses.append(response.json().get("status"))

                if (
                    response.json().get("status") == "COMPLETED"
                    and not response.json().get("id") in ids_messaged
                ):

                    # Notifying the MLOps back-end via Kafka message
                    message = {
                        "type": "RUN_JOB_FINISHED",
                        "job_info": response.json(),
                        "external_pipeline_run_id": kwargs["dag_run"].run_id,
                        "timestamp": datetime.utcnow().isoformat() + "Z",
                    }

                    send_kafka_message(message)

                    ids_messaged.append(response.json().get("id"))

            # TODO: statuses should be in a enum-like entity and the same that we have in the HPC service.
            if all(
                status in {"COMPLETED", "CANCELLED", "FAILED"} for status in statuses
            ):
                return  # TODO: return all_job_ids with corresponding satuses-> will go to XCom.

            time.sleep(polling_interval)

        raise AirflowFailException("Timeout waiting for jobs to finish")

    return PythonOperator(task_id="wait_for_jobs", python_callable=_wait, dag=dag)


# ETL model (DataOps phase; Feature Store Lite)
def extract_soap_vectors(dag):

    def _extract(**kwargs):

        dag_conf = kwargs["dag_run"].conf

        task_conf = dag_conf.get("explore_cells_task", {})
        nominal_composition = task_conf.get("nominal_composition")
        soap_parameters = task_conf.get("soap_parameters")
        runs_jobs = task_conf.get("runs_jobs", [])

        for run in runs_jobs:

            run_number = run.get("run_number")

            payload = soap_parameters

            response = requests.post(
                f"{MS_API_URL}/api/v1/dataops/extract_soap_vectors/{nominal_composition}/{run_number}/0",
                json=payload,
            )

            # TODO: the type is a Java Enum in Spring Boot gateway REST API
            if response.status_code == 200:
                kafka_message_type = "SOAP_VECTORS_EXTRACTED"
            else:
                kafka_message_type = "SOAP_VECTORS_EXTRACTION_FAILED"

            # Notifying the MLOps back-end via Kafka message
            message = {
                "type": kafka_message_type,
                "nominal_composition": nominal_composition,
                "run_number": run_number,
                "sub_run_numbers": [0],
                "external_pipeline_run_id": kwargs["dag_run"].run_id,
                "timestamp": datetime.utcnow().isoformat() + "Z",
            }

            send_kafka_message(message)

            if response.status_code != 200:
                raise AirflowFailException(
                    f"Failed to submit job. URL: {MS_API_URL}/api/v1/dataops/extract_soap_vectors/{nominal_composition}/{run_number}/0\n"
                    f"Payload: {payload}\n"
                    f"Status Code: {response.status_code}\n"
                    f"Response: {response.text}"
                )

    return PythonOperator(
        task_id="extract_soap_vectors", python_callable=_extract, dag=dag
    )


# ETL model (DataOps phase; Feature Store Lite)
def create_pbssdb(dag):

    def _create_pbssdb(**kwargs):

        dag_conf = kwargs["dag_run"].conf

        task_conf = dag_conf.get("explore_cells_task", {})
        nominal_composition = task_conf.get("nominal_composition")
        all_runs_with_sub_runs = task_conf.get("all_runs_with_sub_runs", [])

        payload = {"all_runs_with_sub_runs": all_runs_with_sub_runs}

        response = requests.post(
            f"{MS_API_URL}/api/v1/dataops/create_pbssdb/{nominal_composition}",
            json=payload,
        )

        # TODO: the type is a Java Enum in Spring Boot gateway REST API
        if response.status_code == 200:
            kafka_message_type = "SSDB_CREATED"
        else:
            kafka_message_type = "SSDB_CREATION_FAILED"

        runs_jobs = task_conf.get("runs_jobs", [])
        new_runs_in_pbssdb = []
        for run in runs_jobs:

            run_number = run.get("run_number")

            new_runs_in_pbssdb.append(
                {"run_number": run_number, "sub_run_numbers": [0]}
            )

        # Notifying the MLOps back-end via Kafka message
        message = {
            "type": kafka_message_type,
            "nominal_composition": nominal_composition,
            "new_runs_in_pbssdb": new_runs_in_pbssdb,
            "external_pipeline_run_id": kwargs["dag_run"].run_id,
            "timestamp": datetime.utcnow().isoformat() + "Z",
        }

        send_kafka_message(message)

        if response.status_code != 200:
            raise AirflowFailException(
                f"Failed to submit job. URL: {MS_API_URL}/api/v1/dataops/create_pbssdb/{nominal_composition}\n"
                f"Payload: {payload}\n"
                f"Status Code: {response.status_code}\n"
                f"Response: {response.text}"
            )

    return PythonOperator(
        task_id="create_pbssdb", python_callable=_create_pbssdb, dag=dag
    )
