from airflow import DAG
from datetime import datetime
from tasks.pre_deployment_evaluate_model import evaluate_pbssdb

########################################################################
#
# DAG scoped to the Model Development (ModelOps) phase.
#
########################################################################

with DAG(
    dag_id="pre_deployment_exploration",
    start_date=datetime.utcnow(),
    schedule_interval=None,
    catchup=False,
    max_active_runs=1,  # ensures that only one DAG run will be active at any time
    tags=[  # metadata for categorization/organization of DAGs in the Airflow UI
        "evaluate_model",
        "pre-deployment",
    ],
) as dag:

    # The sequence of tasks execution in this DAG
    step_1 = evaluate_pbssdb(dag)

    step_1
