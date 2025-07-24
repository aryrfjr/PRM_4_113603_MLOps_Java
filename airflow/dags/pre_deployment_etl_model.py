from airflow import DAG
from datetime import datetime
from tasks.pre_deployment_etl_model import extract_soap_vectors, create_pbssdb

########################################################################
#
# DAG scoped to the Data Labeling (DataOps) phase; i.e., the step ETL
# model (DataOps phase; Feature Store Lite) in the MLOps workflow.
#
########################################################################

with DAG(
    dag_id="pre_deployment_etl_model",
    start_date=datetime.utcnow(),
    schedule_interval=None,
    catchup=False,
    max_active_runs=1,  # ensures that only one DAG run will be active at any time
    tags=[  # metadata for categorization/organization of DAGs in the Airflow UI
        "etl_model",
        "pre-deployment",
    ],
) as dag:

    # The sequence of tasks execution in this DAG
    step_1 = extract_soap_vectors(dag)
    step_2 = create_pbssdb(dag)

    step_1 >> step_2
