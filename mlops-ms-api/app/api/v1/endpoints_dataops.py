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
from ase.io import read
from theo4m.io import read_cohp
import os
import json
import pickle as pkl

router = APIRouter()
storage = MinioStorage()

MINIO_BUCKET_NAME = "mlops-bucket"


#
# Uses QUIP (TODO) for feature engineering by computing the feature
# vectors for the ML model (SOAP descriptors) from the LAMMPS .dump file
#
########################################################################
@router.post(
    "/extract_soap_vectors/{nominal_composition}/{run}/{sub_run}",
    response_model=GenericStatusResponse,
    status_code=status.HTTP_200_OK,
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

    # local_file_path = f"/data/ML/big-data-full/{nominal_composition}/c/md/lammps/100/{run}/zca-th300.dump"
    local_file_path = f"/data/ML/big-data-full/{nominal_composition}-SOAPS/c/md/lammps/100/{run}/2000/{sub_run}/SOAPS.vec"
    object_name = f"ML/big-data-full/{nominal_composition}-SOAPS/c/md/lammps/100/{run}/2000/{sub_run}/SOAPS.vec"

    if not os.path.isfile(local_file_path):
        raise HTTPException(
            status_code=404, detail=f"File '{local_file_path}' not found."
        )

    try:

        # TODO: instead of the simple copy process, use QUIP for feature engineering
        #   by computing the feature vectors for the ML model (SOAP descriptors).
        storage.upload_file(MINIO_BUCKET_NAME, object_name, local_file_path)

        return GenericStatusResponse(
            message=f"Uploaded '{object_name}' to MinIO bucket '{MINIO_BUCKET_NAME}'.",
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
    status_code=status.HTTP_200_OK,
    summary="Writes to a per-bond single SOAP database (PBSSDB) directory.",
    tags=["DataOps"],
)
async def create_pbssdb(
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

    # Creating a temporary versioned directory
    pbssdb_version = datetime.utcnow().strftime("V%Y%m%dT%H%M%SZ")

    pbssdb_minio_dir = (
        f"{PBSSDB_OUTPUT_BASE}/{nominal_composition}-PBSSDB-{pbssdb_version}"
    )

    pbssdb_tmp_dir = DirPath(f"/tmp/{pbssdb_minio_dir}")

    pbssdb_tmp_dir.mkdir(parents=True, exist_ok=True)

    # Parse JSON from request
    payload = request.dict()

    # Saving a file with the request payload content just for the record
    payload_file_path = pbssdb_tmp_dir / "request_payload.json"
    with open(payload_file_path, "w") as f:
        json.dump(payload, f, indent=2)  # write dict to file

    # Downloading input files from MinIO to the temporary versioned directory
    for run in payload["all_runs_with_sub_runs"]:

        run_number = run["run_number"]

        for sub_run_number in run["sub_run_numbers"]:

            # The SOAP.vec files
            storage.download_prefix(
                MINIO_BUCKET_NAME,
                f"{PBSSDB_OUTPUT_BASE}/{nominal_composition}-SOAPS/c/md/lammps/100/{run_number}/2000/{sub_run_number}",
                f"/tmp/{PBSSDB_OUTPUT_BASE}/{nominal_composition}-SOAPS/c/md/lammps/100/{run_number}/2000/{sub_run_number}",
            )

            # The sub-Runs output files
            storage.download_prefix(
                MINIO_BUCKET_NAME,
                f"{PBSSDB_OUTPUT_BASE}/{nominal_composition}/c/md/lammps/100/{run_number}/2000/{sub_run_number}",
                f"/tmp/{PBSSDB_OUTPUT_BASE}/{nominal_composition}/c/md/lammps/100/{run_number}/2000/{sub_run_number}",
            )

    # Generating the PBSSDB output files
    create_pbssdb_files(
        f"/tmp/{PBSSDB_OUTPUT_BASE}", nominal_composition, payload, pbssdb_tmp_dir
    )

    # Uploading the temporary versioned directory to MinIO
    try:

        num_files = storage.upload_directory(
            local_dir=pbssdb_tmp_dir,
            bucket_name=MINIO_BUCKET_NAME,
            minio_prefix=pbssdb_minio_dir,
        )

        # TODO: return version so that the next DAG task can create the corresponding mixed DBs
        return GenericStatusResponse(
            message=f"Created PBSSDB version '{pbssdb_version}' in directory '{pbssdb_minio_dir}' ({num_files} files).",
            status=Status.DONE.value,
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


########################################################################
#
# Helpers
#
########################################################################


#
# Creates the PBSSDB output files
#
# TODO: refactor ... the code was basically copied from original work.
#
########################################################################
def create_pbssdb_files(
    base_inputs_dir: str,
    nominal_composition: str,
    payload: dict,
    pbssdb_output_dir: str,
):
    # An information file with those environments in runs with DONE status
    # but containig unmatched bonds
    FDB_DUB = open(
        f"{pbssdb_output_dir}/DUB.info", "w"
    )  # TODO: will stay empty for now
    # An information file with those <SUB_STEP>s with a non-DONE status
    FDB_ND = open(f"{pbssdb_output_dir}/ND.info", "w")  # TODO: will stay empty for now

    # Per-bond-type .bnd big files with ICOHP information
    FDB_DONE_ZrZr = open(f"{pbssdb_output_dir}/Zr-Zr.bnd", "w")
    FDB_DONE_CuCu = open(f"{pbssdb_output_dir}/Cu-Cu.bnd", "w")
    FDB_DONE_AlAl = open(f"{pbssdb_output_dir}/Al-Al.bnd", "w")
    FDB_DONE_ZrCu = open(f"{pbssdb_output_dir}/Zr-Cu.bnd", "w")
    FDB_DONE_ZrAl = open(f"{pbssdb_output_dir}/Zr-Al.bnd", "w")
    FDB_DONE_CuAl = open(f"{pbssdb_output_dir}/Cu-Al.bnd", "w")

    # And also all SOAP vectors as dictionaries
    ALL_SOAPS_ZrZr = {}
    ALL_SOAPS_CuCu = {}
    ALL_SOAPS_AlAl = {}
    ALL_SOAPS_ZrCu = {}
    ALL_SOAPS_ZrAl = {}
    ALL_SOAPS_CuAl = {}

    SS_SOAPS = None  # <SUB_STEP>s (or sub-Runs) SOAPs

    for run in payload["all_runs_with_sub_runs"]:

        run_number = run["run_number"]

        for sub_run_number in run["sub_run_numbers"]:

            sub_run_dir = f"{base_inputs_dir}/{nominal_composition}/c/md/lammps/100/{run_number}/2000/{sub_run_number}"
            sub_run_soaps_dir = f"{base_inputs_dir}/{nominal_composition}-SOAPS/c/md/lammps/100/{run_number}/2000/{sub_run_number}"

            with open(f"{sub_run_soaps_dir}/SOAPS.vec", "rb") as ftl:
                SS_SOAPS = pkl.load(ftl, encoding="latin1")
                ftl.close()

            # Reading ICOHPLIST.lobster
            bonds = read_cohp(
                cohpfile=f"{sub_run_dir}/ICOHPLIST.lobster", task="ICOHPLIST"
            )

            # Reading the .xyz file
            atoms = read(f"{sub_run_dir}/{nominal_composition}.xyz")
            cell = atoms.get_cell()
            pos = atoms.get_positions()
            symbs = atoms.get_chemical_symbols()

            # Reading the reference file with the clusters
            fileobj = open(f"{sub_run_dir}/lobsterin-quippy")
            lines = fileobj.readlines()
            fileobj.close()

            # Setting up each one
            lastA = -1
            bonds_to_writeK = []  # used only with a per-bond database
            for i in range(9, len(lines)):
                refA = int(lines[i].split()[2])
                refB = int(lines[i].split()[4])
                if refA != lastA:
                    if i > 9:
                        bonds_to_writeK = eval_cluster(
                            bonds,
                            acs,
                            symbs,
                            bonds_to_writeK,
                            run_number,
                            sub_run_number,
                            SS_SOAPS,
                            FDB_DONE_ZrZr,
                            ALL_SOAPS_ZrZr,
                            FDB_DONE_CuCu,
                            ALL_SOAPS_CuCu,
                            FDB_DONE_AlAl,
                            ALL_SOAPS_AlAl,
                            FDB_DONE_ZrCu,
                            ALL_SOAPS_ZrCu,
                            FDB_DONE_ZrAl,
                            ALL_SOAPS_ZrAl,
                            FDB_DONE_CuAl,
                            ALL_SOAPS_CuAl,
                            FDB_DUB,
                        )
                    acs = []
                    acs.append(refA)
                    offset = []
                acs.append(refB)
                offset.append(
                    [
                        int(lines[i].split()[6]),
                        int(lines[i].split()[7]),
                        int(lines[i].split()[8]),
                    ]
                )
                lastA = refA

                # Setting up the last
                bonds_to_writeK = eval_cluster(
                    bonds,
                    acs,
                    symbs,
                    bonds_to_writeK,
                    run_number,
                    sub_run_number,
                    SS_SOAPS,
                    FDB_DONE_ZrZr,
                    ALL_SOAPS_ZrZr,
                    FDB_DONE_CuCu,
                    ALL_SOAPS_CuCu,
                    FDB_DONE_AlAl,
                    ALL_SOAPS_AlAl,
                    FDB_DONE_ZrCu,
                    ALL_SOAPS_ZrCu,
                    FDB_DONE_ZrAl,
                    ALL_SOAPS_ZrAl,
                    FDB_DONE_CuAl,
                    ALL_SOAPS_CuAl,
                    FDB_DUB,
                )

    # OK, done with the big .bnd files
    FDB_DONE_ZrZr.close()
    FDB_DONE_CuCu.close()
    FDB_DONE_AlAl.close()
    FDB_DONE_ZrCu.close()
    FDB_DONE_ZrAl.close()
    FDB_DONE_CuAl.close()

    # And finally dumping the per-element SOAP vectors synchronized with the respective .xyz files
    ftd = open(f"{pbssdb_output_dir}/Zr-Zr-SOAPS.vec", "wb")
    pkl.dump(ALL_SOAPS_ZrZr, ftd)
    ftd.close()
    ftd = open(f"{pbssdb_output_dir}/Cu-Cu-SOAPS.vec", "wb")
    pkl.dump(ALL_SOAPS_CuCu, ftd)
    ftd.close()
    ftd = open(f"{pbssdb_output_dir}/Al-Al-SOAPS.vec", "wb")
    pkl.dump(ALL_SOAPS_AlAl, ftd)
    ftd.close()
    ftd = open(f"{pbssdb_output_dir}/Zr-Cu-SOAPS.vec", "wb")
    pkl.dump(ALL_SOAPS_ZrCu, ftd)
    ftd.close()
    ftd = open(f"{pbssdb_output_dir}/Zr-Al-SOAPS.vec", "wb")
    pkl.dump(ALL_SOAPS_ZrAl, ftd)
    ftd.close()
    ftd = open(f"{pbssdb_output_dir}/Cu-Al-SOAPS.vec", "wb")
    pkl.dump(ALL_SOAPS_CuAl, ftd)
    ftd.close()

    # Finishing
    FDB_DUB.close()
    FDB_ND.close()


#
# Evaluates individual local environments around central atoms
#
# TODO: refactor ... the code was basically copied from original work.
#
########################################################################
def eval_cluster(
    bonds,
    acs,
    symbs,
    bonds_to_writeK,
    id_run,
    sub_step,
    SS_SOAPS,
    FDB_DONE_ZrZr,
    ALL_SOAPS_ZrZr,
    FDB_DONE_CuCu,
    ALL_SOAPS_CuCu,
    FDB_DONE_AlAl,
    ALL_SOAPS_AlAl,
    FDB_DONE_ZrCu,
    ALL_SOAPS_ZrCu,
    FDB_DONE_ZrAl,
    ALL_SOAPS_ZrAl,
    FDB_DONE_CuAl,
    ALL_SOAPS_CuAl,
    FDB_DUB,
):
    symbA = symbs[acs[0] - 1]
    knf = "KNF: "  # keys not found
    bonds_to_write = []  # used only with a per-bond database

    for ib in range(1, len(acs)):
        symbB = symbs[acs[ib] - 1]
        key = symbA + str(acs[0]) + "-" + symbB + str(acs[ib])
        nknf = ""
        try:  # trying with the normal key AtomA-AtomB
            bond = bonds[key]
        except KeyError:
            nknf = key
            try:  # trying with the inverted key AtomB-AtomA
                key = symbB + str(acs[ib]) + "-" + symbA + str(acs[0])
                bond = bonds[key]
                nknf = ""
            except KeyError:  # none of the two keys have been found
                nknf = key
        if nknf == "":  # if at least one of the two keys have been found
            if (
                not bond.get_symbA() + "-" + bond.get_symbB() in bonds_to_writeK
                and not bond.get_symbB() + "-" + bond.get_symbA() in bonds_to_writeK
            ):
                bonds_to_writeK.append(bond.get_symbA() + "-" + bond.get_symbB())
                bonds_to_write.append(bond)
        else:
            knf = knf + nknf + ", "

    if knf == "KNF: ":  # ok! The LOBSTER and quippy clusters are compatible ...
        for ibnd in range(len(bonds_to_write)):
            bond = bonds_to_write[ibnd]
            symbA = bond.get_symbA()[:2]
            symbB = bond.get_symbB()[:2]
            if symbA + symbB == "ZrZr":
                ftw = FDB_DONE_ZrZr  # file to write
                stl = ALL_SOAPS_ZrZr  # SOAPs to load
            elif symbA + symbB == "CuCu":
                ftw = FDB_DONE_CuCu
                stl = ALL_SOAPS_CuCu
            elif symbA + symbB == "AlAl":
                ftw = FDB_DONE_AlAl
                stl = ALL_SOAPS_AlAl
            elif symbA + symbB == "ZrCu" or symbA + symbB == "CuZr":
                ftw = FDB_DONE_ZrCu
                stl = ALL_SOAPS_ZrCu
            elif symbA + symbB == "ZrAl" or symbA + symbB == "AlZr":
                ftw = FDB_DONE_ZrAl
                stl = ALL_SOAPS_ZrAl
            elif symbA + symbB == "CuAl" or symbA + symbB == "AlCu":
                ftw = FDB_DONE_CuAl
                stl = ALL_SOAPS_CuAl
            # ... write the .bnd file and ...
            ftw.write(
                "%d %d %s %s %f %f\n"
                % (
                    int(id_run),
                    sub_step,
                    int(bond.get_symbA()[2:]),
                    int(bond.get_symbB()[2:]),
                    bond.get_distance(),
                    -bond.get_info(),
                )
            )
            # ... saving the SOAP vectors in a dictionary
            # to avoid repeated elements
            keysoap = f"{id_run}-{sub_step}-{bond.get_symbA()[2:]}"
            if not keysoap in stl.keys():
                stl[keysoap] = SS_SOAPS[int(bond.get_symbA()[2:]) - 1]
            keysoap = f"{id_run}-{sub_step}-{bond.get_symbB()[2:]}"
            if not keysoap in stl.keys():
                stl[keysoap] = SS_SOAPS[int(bond.get_symbB()[2:]) - 1]
    else:
        # ... otherwise, I will write the unmatched bonds (UBs)
        # and this cluster will be discarded even with a DONE status.
        # It is IMPORTANT to point that these are bonds that were
        # detected only by quippy (see more on 20/06/2019-(6)). Bonds
        # that were detected only by LOBSTER are naturally discarded
        # since the quippy clusters are those with which the SOAP
        # vectors are generated and I'm supposed to have the ICOHP
        # values for all bonds in the clusters.
        FDB_DUB.write("%d %d %s\n" % (int(id_run), sub_step, knf[: len(knf) - 2]))

    return bonds_to_writeK  # used only with a per-bond database
