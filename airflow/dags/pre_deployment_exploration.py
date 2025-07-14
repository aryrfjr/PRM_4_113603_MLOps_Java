from airflow import DAG
from datetime import datetime
from tasks.pre_deployment_tasks import (
    submit_jobs,
    wait_for_jobs,
    extract_soap_vectors,
    create_ssdb,
)

########################################################################
#
# DAG scoped to the Data Generation & Labeling (DataOps) and Model
# Development (ModelOps) phases, which includes the following steps:
#
# - Generate (DataOps phase; exploration)
# - ETL model (DataOps phase; Feature Store Lite)
#
########################################################################

with DAG(
    dag_id="pre_deployment_exploration",
    start_date=datetime.utcnow(),
    schedule_interval=None,
    catchup=False,
    tags=[  # metadata for categorization/organization of DAGs in the Airflow UI
        "explore",
        "pre-deployment",
    ],
) as dag:

    # The sequence of tasks execution in this DAG
    step_1 = submit_jobs(dag)
    step_2 = wait_for_jobs(dag)
    step_3 = extract_soap_vectors(dag)
    step_4 = create_ssdb(dag)

    step_1 >> step_2 >> step_3 >> step_4
