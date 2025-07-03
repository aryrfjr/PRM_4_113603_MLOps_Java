from fastapi import APIRouter, HTTPException, status, Path, Body
from datetime import datetime
from app.schemas import (
    GenericStatusResponse,
    Status,
    ExtractSOAPVectorsRequest,
    CreatePBSSDBRequest,
)
from app.services.minio_client import MinioStorage
from pathlib import Path as DirPath
import os
import json

router = APIRouter()
storage = MinioStorage()

BUCKET_NAME = "mlops-bucket"


#
# Uses QUIP (TODO) for feature engineering by computing the feature
# vectors for the ML model (SOAP descriptors) from the LAMMPS .dump file
#
########################################################################
@router.post(
    "/extract_soap_vectors/{nominal_composition}/{run}/{sub_run}",
    response_model=GenericStatusResponse,
    status_code=status.HTTP_202_ACCEPTED,
    summary="Computes the feature vectors for the ML model (SOAP descriptors).",
    tags=["DataOps"],
)
def extract_soap_vectors(
    nominal_composition: str = Path(
        ..., description="Nominal Composition (e.g., Zr47Cu47Al6)"
    ),
    run: int = Path(..., ge=1, description="Run number (not ID; e.g., 1)."),
    sub_run: int = Path(
        ..., ge=0, le=14, description="sub-Run number (not ID; between 0 and 14)."
    ),
    request: ExtractSOAPVectorsRequest = Body(
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
    object_name = f"ML/big-data-full/{nominal_composition}-SOAPS/c/md/lammps/100/{run}/2000/{sub_run}/SOAPS.vec"

    if not os.path.isfile(local_file_path):
        raise HTTPException(
            status_code=404, detail=f"File '{local_file_path}' not found."
        )

    try:

        # TODO: instead of the simple copy process, use QUIP for feature engineering
        #   by computing the feature vectors for the ML model (SOAP descriptors).
        storage.upload_file(BUCKET_NAME, object_name, local_file_path)

        return GenericStatusResponse(
            message=f"Uploaded '{object_name}' to bucket '{BUCKET_NAME}'.",
            status=Status.DONE.value,
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


#
# Writes to a per-bond single SOAP database (PBSSDB) directory.
#
########################################################################
@router.post(
    "/create_ssdb/{nominal_composition}",
    response_model=GenericStatusResponse,
    status_code=status.HTTP_202_ACCEPTED,
    summary="Writes to a per-bond single SOAP database (PBSSDB) directory.",
    tags=["DataOps"],
)
def create_pbssdb(
    nominal_composition: str = Path(
        ..., description="Nominal Composition (e.g., Zr47Cu47Al6)"
    ),
    request: CreatePBSSDBRequest = Body(
        ..., description="Parameters for PBSSDB creation."
    ),
):
    """
    Writes to a per-bond single SOAP database (PBSSDB) directory
    'ML/big-data-full/<NOMINAL_COMPOSITION>-PBSSDB' files for all possible bond-types.
    """

    PBSSDB_OUTPUT_BASE = "ML/big-data-full"

    # The versioned dir under /tmp
    timestamp = datetime.utcnow().strftime("V%Y%m%dT%H%M%SZ")
    pbssdb_tmp_dir = DirPath(
        f"/tmp/{PBSSDB_OUTPUT_BASE}/{nominal_composition}-PBSSDB-{timestamp}"
    )
    pbssdb_tmp_dir.mkdir(parents=True, exist_ok=True)

    # Saving a file with the request payload content
    payload = request.json()  # TODO: use await and define create_pbssdb as async?

    payload_file_path = pbssdb_tmp_dir / "request_payload.json"
    with open(payload_file_path, "w") as f:
        json.dump(payload, f, indent=2)

    try:

        pbssdb_minio_dir = (
            f"{PBSSDB_OUTPUT_BASE}/{nominal_composition}-PBSSDB-{timestamp}"
        )

        num_files = storage.upload_directory(
            local_dir=pbssdb_tmp_dir,
            bucket_name="mlops-bucket",
            minio_prefix=pbssdb_minio_dir,
        )

        return GenericStatusResponse(
            message=f"Uploaded '{num_files}' file(s) to bucket '{BUCKET_NAME}'.",
            status=Status.DONE.value,
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
