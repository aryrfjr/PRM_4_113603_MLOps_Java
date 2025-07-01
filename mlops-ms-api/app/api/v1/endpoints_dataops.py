from fastapi import APIRouter

##########################################################################
#
# Globals
#
##########################################################################

router = APIRouter()

##########################################################################
#
# Helpers
#
##########################################################################


#
# Endpoints scoped to the Data Generation & Labeling (DataOps) phase,
# which includes the following steps:
#
# - Generate (DataOps phase; exploration/exploitation)
# - ETL model (DataOps phase; Feature Store Lite)
#
########################################################################
