from airflow.operators.python import PythonOperator
from airflow.exceptions import AirflowFailException
import os
import requests
import time

##########################################################################
#
# Globals
#
##########################################################################

# NOTE: the MS_API_URL environment variable was defined in docker-compose.yml
HPC_API_URL = os.getenv("HPC_API_URL")
MS_API_URL = os.getenv("MS_API_URL")

##########################################################################
#
# Helpers
#
##########################################################################

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
        # nominal_composition = task_conf.get("nominal_composition") # TODO: must with the Kafka message to mlops-api
        runs_jobs = task_conf.get("runs_jobs", [])

        all_job_ids = []
        for run in runs_jobs:

            # run_id = run.get("run_id") # TODO: must with the Kafka message to mlops-api
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
                    raise AirflowFailException(
                        f"Failed to submit job. URL: {HPC_API_URL}/api/v1/jobs\n"
                        f"Payload: {payload}\n"
                        f"Status Code: {response.status_code}\n"
                        f"Response: {response.text}"
                    )

                current_job_id = response.json().get("id")
                all_job_ids.append(current_job_id)

                previous_job_id = current_job_id  # next job will depend on this one

        # return job_ids -> will go to XCom
        return all_job_ids

    return PythonOperator(task_id="submit_jobs", python_callable=_submit, dag=dag)


# Generate (DataOps phase; exploration/exploitation)
def wait_for_jobs(dag):

    def _wait(**kwargs):

        # NOTE: Getting access to the Task Instance object. It represents the current
        #   execution of a task inside a DAG run. It's injected automatically into PythonOperator
        #   functions that accept **kwargs.
        ti = kwargs["ti"]

        # NOTE: xcom_pull is a method on the TaskInstance object (ti) that allows one
        #   to retrieve data shared by a previous task via XCom (short for “Cross-Communication”).
        job_ids = ti.xcom_pull(task_ids="submit_jobs")
        if not job_ids:
            raise AirflowFailException("No job IDs found in XCom")

        polling_interval = 10  # seconds; TODO: should go to docker-compose.yml
        max_retries = 360  # retry for 1 hour max (360 * 10s); TODO: should go to docker-compose.yml

        for attempt in range(max_retries):

            statuses = []
            for job_id in job_ids:

                response = requests.get(f"{HPC_API_URL}/api/v1/jobs/{job_id}")
                if response.status_code != 200:
                    raise AirflowFailException(
                        f"Failed to submit job. URL: {HPC_API_URL}/api/v1/jobs/{job_id}\n"
                        f"Status Code: {response.status_code}\n"
                        f"Response: {response.text}"
                    )

                statuses.append(response.json().get("status"))

            if all(status == "COMPLETED" for status in statuses):
                return

            time.sleep(polling_interval)

        raise AirflowFailException("Timeout waiting for jobs to finish")

    return PythonOperator(task_id="wait_for_jobs", python_callable=_wait, dag=dag)


# ETL model (DataOps phase; Feature Store Lite)
def etl_model(dag):

    def _etl():

        print(f"_etl")

    return PythonOperator(task_id="etl_model", python_callable=_etl, dag=dag)
