from fastapi import APIRouter, HTTPException, status, Path, Body
from app.schemas import (
    GenericStatusResponse,
    Status,
    ExtractSoapsRequest,
    ExtractSoapsResponse,
)
from app.services.minio_client import MinioStorage
import os

router = APIRouter()
storage = MinioStorage()


#
# Uses QUIP for feature engineering by computing the feature vectors
# for the ML model (SOAP descriptors) from the LAMMPS .dump file
#
########################################################################
@router.post(
    "/extract_soaps/{nominal_composition}/{run}/{sub_run}",
    response_model=GenericStatusResponse,
    status_code=status.HTTP_202_ACCEPTED,
    summary="Computes the feature vectors for the ML model (SOAP descriptors).",
    tags=["DataOps"],
)
def extract_soaps(
    nominal_composition: str = Path(
        ..., description="Nominal Composition (e.g., Zr47Cu47Al6)"
    ),
    run: int = Path(..., ge=1, description="Run number (not ID; e.g., 1)."),
    sub_run: int = Path(
        ..., ge=0, le=14, description="SubRun number (not ID; between 0 and 14)."
    ),
    request: ExtractSoapsRequest = Body(
        ..., description="Parameters for SOAP vectors calculations."
    ),
):
    """
    Uses QUIP for feature engineering by computing the feature vectors
    for the ML model (SOAP descriptors) from the LAMMPS **.dump** file.
    """

    # TODO: since SOAPs are not being generated right now, should include
    #   a delay command to wait for a while before upload the SOAPs.vec file.

    local_file_path = f"/data/ML/big-data-full/{nominal_composition}/c/md/lammps/100/{run}/zca-th300.dump"
    bucket_name = "mlops-bucket"
    object_name = f"ML/big-data-full/{nominal_composition}-SOAPS/c/md/lammps/100/{run}/2000/{sub_run}/SOAPS.vec"

    if not os.path.isfile(local_file_path):
        raise HTTPException(
            status_code=404, detail=f"File '{local_file_path}' not found."
        )

    try:

        # TODO: instead of the simple copy process, use QUIP for feature engineering
        #   by computing the feature vectors for the ML model (SOAP descriptors).
        storage.upload_file(bucket_name, object_name, local_file_path)

        return ExtractSoapsResponse(
            message=f"Uploaded '{object_name}' to bucket '{bucket_name}'.",
            status=Status.DONE.value,
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
