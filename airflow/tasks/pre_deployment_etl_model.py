from datetime import datetime
from airflow.operators.python import PythonOperator
from airflow.exceptions import AirflowFailException
import os
import requests
from utils.kafka_utils import send_kafka_message

##########################################################################
#
# Globals
#
##########################################################################

#
# Environment variables defined in docker-compose.yml
#
##########################################################################

# Internal Docker network communication with the MLOps Microservices API
MS_API_URL = os.getenv("MS_API_URL")

##########################################################################
#
# Helpers
#
##########################################################################


#
# DAG Tasks scoped to the Data Labeling (DataOps) phase; i.e., the step
# ETL model (DataOps phase; Feature Store Lite) in the MLOps workflow.
#
########################################################################


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
