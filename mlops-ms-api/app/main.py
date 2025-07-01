from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app import schemas
from .api.v1 import endpoints_dataops as v1_endpoints_dataops
from .api.v1 import endpoints_modelops as v1_endpoints_modelops

#
# Initialize FastAPI App
#
#######################################################################

API_NAME = "REST API Microservices for the MLOps workflow used in Phys. Rev. Materials 4, 113603"

# Define OpenAPI tag groups
tags_metadata = [
    {
        "name": "DataOps",
        "description": "Tasks related to ETL/DBI creation.",
    },
    {
        "name": "ModelOps",
        "description": "Tasks related to evaluating trained models on DBI test sets.",
    },
    {
        "name": "Misc",
        "description": "Miscellaneous.",
    },
]

app = FastAPI(
    title=API_NAME,
    version="1.0.0",
    description="REST API Microservices for DataOps and ModelOps tasks feature store (DBIs) and model training/evaluation.",
    openapi_tags=tags_metadata,
)

#
# CORS Middleware (optional for local dev or frontend apps)
#
#######################################################################
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Change in production to specific domains
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

#
# Endpoints for different resources types and scopes
#
# TODO: review status codes.
#
# TODO: review error handling: consistent and descriptive error responses,
#  with machine-readable codes and human-readable messages.
#
# TODO: Authentication (API key or OAuth2 support).
#
# TODO: Event-driven trigger (API could emit events, like job completed,
#  to Airflow or MLflow to trigger downstream tasks.
#
# TODO: General refactoring for all endpoints following the MVC Separation
#   of Concerns the Controller only handles HTTP protocol, request validation,
#   and response formatting; whereas the Service implements domain/business
#   logic, the Repository manages persistence, and the Model stays in the data layer.
#
##########################################################################

#
# API Routers
#
#######################################################################
app.include_router(v1_endpoints_dataops.router, prefix="/api/v1/dataops")
app.include_router(v1_endpoints_modelops.router, prefix="/api/v1/modelops")


#
# Health Check Endpoint
#
#######################################################################
@app.get("/")
def read_root():
    return {
        "message": f"{API_NAME} is running.",
        "status": "OK",
    }


#
# Miscellaneous
#
########################################################################


@app.get("/ping", response_model=schemas.PingResponse, tags=["Misc"])
def ping():
    return {"message": "PING OK"}
