from airflow import DAG
from datetime import datetime
from tasks.pre_deployment_tasks import submit_jobs, wait_for_jobs, etl_model

########################################################################
#
# DAG scoped to the Data Generation & Labeling (DataOps) and Model
# Development (ModelOps) phases, which includes the following steps:
#
# - Generate (DataOps phase; exploration)
# - ETL model (DataOps phase; Feature Store Lite)
#
#
#
########################################################################

with DAG(
    dag_id="pre_deployment_exploration",
    start_date=datetime(2024, 1, 1),
    schedule_interval=None,
    catchup=False,
    tags=["explore", "pre-deployment"],
) as dag:

    step_1 = submit_jobs(dag)
    step_2 = wait_for_jobs(dag)
    step_3 = etl_model(dag)

    step_1 >> step_2 >> step_3
