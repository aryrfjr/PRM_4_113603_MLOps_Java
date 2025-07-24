from airflow.operators.python import PythonOperator
import os


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


#
# DAG Tasks scoped to the Model Development (ModelOps) phase.
#
########################################################################


# Evaluate PBSSDB.
def evaluate_pbssdb(dag):

    def _evaluate_pbssdb(**kwargs):

        dag_conf = kwargs["dag_run"].conf

        return

    return PythonOperator(
        task_id="create_pbssdb", python_callable=_evaluate_pbssdb, dag=dag
    )
