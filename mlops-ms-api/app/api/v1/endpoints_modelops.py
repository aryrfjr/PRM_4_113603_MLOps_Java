from fastapi import APIRouter, HTTPException, status, Body
from app.schemas import (
    GenericStatusResponse,
    Status,
    PBSSDBEvaluationRequest,
)
from app.services.minio_client import MinioStorage
from random import shuffle
import pickle as pkl
import numpy as np

##########################################################################
#
# Globals
#
##########################################################################

router = APIRouter()
storage = MinioStorage()

MINIO_BUCKET_NAME = "mlops-bucket"

#
# Endpoints scoped to the Model Development (ModelOps) phase,
# which includes the step:
#
# - Train/Tune (observability or model evaluation in the ModelOps phase):
#
#   - Per-bond type evaluation with training and the testing sets from
#     one or more PBSSDB directories.
#
#   - Evaluation in the same scenario of Production; with The training
#     set is loaded from a mixed database, whereas the testing set is
#     loaded from a 100-atom cell of a specific Nominal Composition.
#
########################################################################


#
# Evaluates a per-bond single SOAP database (PBSSDB).
#
# TODO: refactor ... the code was basically copied from original work.
#
########################################################################
@router.post(
    "/evaluate_pbssdb",
    response_model=GenericStatusResponse,
    status_code=status.HTTP_200_OK,
    summary="Evaluates a per-bond single SOAP database (PBSSDB).",
    tags=["ModelOps"],
)
async def create_pbssdb(
    request: PBSSDBEvaluationRequest = Body(
        ..., description="Parameters for PBSSDB evaluation."
    ),
):
    """
    Evaluates a per-bond single SOAP database (PBSSDB) directory.
    """

    PBSSDB_DIR_BASE = "ML/big-data-full"

    # Parse JSON from request
    payload = request.dict()

    # Primary input information
    CROSS = payload["cross_evaluation"]
    nominal_compositions = payload["nominal_compositions"]
    pbssdb_versions = payload["pbssdb_versions"]

    if CROSS:  # use one database for testing and another one for training
        CHEM_COMPOSITION_TRAIN = nominal_compositions[0]
        CHEM_COMPOSITION_TEST = nominal_compositions[1]
        PBSSDB_VERSION_TRAIN = pbssdb_versions[0]
        PBSSDB_VERSION_TEST = pbssdb_versions[1]
    else:  # training and testing sets from the same database
        CHEM_COMPOSITION_TRAIN = nominal_compositions[0]
        CHEM_COMPOSITION_TEST = nominal_compositions[0]
        PBSSDB_VERSION_TRAIN = pbssdb_versions[0]
        PBSSDB_VERSION_TEST = pbssdb_versions[0]

    SPECIE_A = payload["specie_a"]
    SPECIE_B = payload["specie_b"]
    KERNEL_DIM = payload["training_set_size"]
    TEST_SIZE = payload["testing_set_size"]
    KERNEL_TYPE = payload["kernel_type"]
    ZETA = payload["zeta"]
    SIGMA = payload["sigma"]
    REGP = payload["regp"]

    DIR_TO_WRITE = ""

    # Downloading the PBSSDBs from MinIO
    for nominal_composition, pbssdb_version in zip(
        nominal_compositions, pbssdb_versions
    ):
        storage.download_prefix(
            MINIO_BUCKET_NAME,
            f"{PBSSDB_DIR_BASE}/{nominal_composition}-PBSSDB-{pbssdb_version}",
            f"/tmp/{PBSSDB_DIR_BASE}/{nominal_composition}-PBSSDB-{pbssdb_version}",
        )

    # Loading the bonds of the training and testing sets and their respective SOAPs
    DB_DIR = (
        f"/tmp/{PBSSDB_DIR_BASE}/{CHEM_COMPOSITION_TRAIN}-PBSSDB-{PBSSDB_VERSION_TRAIN}"
    )
    fileobj = open(f"{DB_DIR}/{SPECIE_A}-{SPECIE_B}.bnd")
    bondsff = fileobj.readlines()
    fileobj.close()

    # shuffling the database before using it
    shuffle(bondsff)

    if not CROSS and KERNEL_DIM + TEST_SIZE > len(bondsff):
        raise HTTPException(
            status_code=404,
            detail="The sum <KERNEL_DIM>+<TEST_SIZE> is greater than the total number of bonds available in the database.",
        )
    elif KERNEL_DIM > len(bondsff):
        raise HTTPException(
            status_code=404,
            detail="The <KERNEL_DIM> is greater than the total number of bonds available in the training database.",
        )
    BONDS_TRAIN = bondsff[:KERNEL_DIM]
    ftl = open(f"{DB_DIR}/{SPECIE_A}-{SPECIE_B}-SOAPS.vec", "rb")
    SOAPS_TRAIN = pkl.load(ftl)
    ftl.close()

    if CROSS:
        DB_DIR = f"/tmp/{PBSSDB_DIR_BASE}/{CHEM_COMPOSITION_TEST}-PBSSDB-{PBSSDB_VERSION_TEST}"
        fileobj = open(f"{DB_DIR}/{SPECIE_A}-{SPECIE_B}.bnd")
        bondsff = fileobj.readlines()
        fileobj.close()
        # shuffling the database before using it
        shuffle(bondsff)
        if TEST_SIZE > len(bondsff):
            raise HTTPException(
                status_code=404,
                detail="The <TEST_SIZE> is greater than the total number of bonds available in the testing database.",
            )

    BONDS_TEST = bondsff[len(bondsff) - TEST_SIZE :]
    if CROSS:
        ftl = open(f"{DB_DIR}/{SPECIE_A}-{SPECIE_B}-SOAPS.vec", "rb")
        SOAPS_TEST = pkl.load(ftl)
        ftl.close()
    else:
        SOAPS_TEST = SOAPS_TRAIN  # reference? or duplicated?

    # Building the kernel matrix
    KM = np.zeros((KERNEL_DIM, KERNEL_DIM))
    for i in range(KERNEL_DIM):

        bi_dist = float(BONDS_TRAIN[i].split()[4])

        soapAi = SOAPS_TRAIN[
            f"{BONDS_TRAIN[i].split()[0]}-{BONDS_TRAIN[i].split()[1]}-{BONDS_TRAIN[i].split()[2]}"
        ]

        soapBi = SOAPS_TRAIN[
            f"{BONDS_TRAIN[i].split()[0]}-{BONDS_TRAIN[i].split()[1]}-{BONDS_TRAIN[i].split()[3]}"
        ]

        for j in range(KERNEL_DIM):

            bj_dist = float(BONDS_TRAIN[j].split()[4])

            soapAj = SOAPS_TRAIN[
                f"{BONDS_TRAIN[j].split()[0]}-{BONDS_TRAIN[j].split()[1]}-{BONDS_TRAIN[j].split()[2]}"
            ]

            soapBj = SOAPS_TRAIN[
                f"{BONDS_TRAIN[j].split()[0]}-{BONDS_TRAIN[j].split()[1]}-{BONDS_TRAIN[j].split()[3]}"
            ]

            dpAAij = np.dot(soapAi, soapAj)
            dpBBij = np.dot(soapBi, soapBj)
            dpABij = np.dot(soapAi, soapBj)
            dpBAij = np.dot(soapBi, soapAj)

            if KERNEL_TYPE == "SE":
                KM[i][j] = np.exp(
                    (-((bi_dist - bj_dist) ** 2)) / (2 * SIGMA**2)
                )  # Squared Exponential (SE)
            elif KERNEL_TYPE == "OU":
                KM[i][j] = np.exp(
                    -abs(bi_dist - bj_dist) / SIGMA
                )  # Ornstein-Uhlenbeck (OU)
            else:
                KM[i][j] = (
                    np.exp((-((bi_dist - bj_dist) ** 2)) / (2 * SIGMA**2))
                    * (0.25 * (dpAAij + dpBBij + dpABij + dpBAij)) ** ZETA
                )

    # Now the variance and standard deviation of the -ICOHP values in the testing set
    ICOHPs_TRAIN = []
    for i in range(KERNEL_DIM):
        ICOHPs_TRAIN.append(float(BONDS_TRAIN[i].split()[5]))
    STD = np.std(ICOHPs_TRAIN)
    VAR = STD**2

    # Now setting up equation (3) in the supplementary material of [Nature Communications 9, 4501 (2018)]
    # Firstly the inverse matrix ... on 09/07/2019 Prof. Gabor said to do this
    IREG = STD * REGP * np.identity(KERNEL_DIM)

    # I tried to use ZETA according to [Nature Communications 9, 4501 (2018)], but on 09/07/2019 Prof. Gabor said to do this
    IKM = np.linalg.inv(KM + IREG)

    # ... and then weights (alpha_i)
    alpha = [0.0] * KERNEL_DIM
    for i in range(KERNEL_DIM):
        for j in range(KERNEL_DIM):
            alpha[i] += IKM.item((i, j)) * ICOHPs_TRAIN[j]

    # And finally the predicted sum(-ICOHP) values for the test set
    if DIR_TO_WRITE != "":
        if CROSS:
            INFO_OUTPUT_PREFIX = f"{CHEM_COMPOSITION_TRAIN}-{CHEM_COMPOSITION_TEST}-"
        else:
            INFO_OUTPUT_PREFIX = f"{CHEM_COMPOSITION_TRAIN}-{CHEM_COMPOSITION_TRAIN}-"

        INFO_OUTPUT_PREFIX += (
            f"{SPECIE_A}-{SPECIE_B}-{KERNEL_DIM}-{ZETA}-{SIGMA}-{REGP}-{TEST_SIZE}"
        )

        ftw = open(f"{DIR_TO_WRITE}/{INFO_OUTPUT_PREFIX}.info", "w")
        dtpx = []
        dtpy = []
        higher = 0.0

    # Now testing
    ICOHPs_TEST = [0.0] * TEST_SIZE
    SUMDIFF = 0.0

    # The testing set will be the lattest BONDS_TEST in the array
    tit = 0  # the true index in the array ICOHPs_TEST
    for it in range(TEST_SIZE):

        bit_dist = float(BONDS_TEST[it].split()[4])

        soapAit = SOAPS_TEST[
            f"{BONDS_TEST[it].split()[0]}-{BONDS_TEST[it].split()[1]}-{BONDS_TEST[it].split()[2]}"
        ]

        soapBit = SOAPS_TEST[
            f"{BONDS_TEST[it].split()[0]}-{BONDS_TEST[it].split()[1]}-{BONDS_TEST[it].split()[3]}"
        ]

        for i in range(KERNEL_DIM):

            bi_dist = float(BONDS_TRAIN[i].split()[4])

            soapAi = SOAPS_TRAIN[
                f"{BONDS_TRAIN[i].split()[0]}-{BONDS_TRAIN[i].split()[1]}-{BONDS_TRAIN[i].split()[2]}"
            ]

            soapBi = SOAPS_TRAIN[
                f"{BONDS_TRAIN[i].split()[0]}-{BONDS_TRAIN[i].split()[1]}-{BONDS_TRAIN[i].split()[3]}"
            ]

            dpAAiti = np.dot(soapAit, soapAi)
            dpBBiti = np.dot(soapBit, soapBi)
            dpABiti = np.dot(soapAit, soapBi)
            dpBAiti = np.dot(soapBit, soapAi)

            # Here I"m using ZETA as suggested by Prof. Gabor on 05/07/2019
            if KERNEL_TYPE == "SE":  # Squared Exponential (SE)
                KERNEL = np.exp((-((bit_dist - bi_dist) ** 2)) / (2 * SIGMA**2))
            elif KERNEL_TYPE == "OU":  # Ornstein-Uhlenbeck (OU)
                KERNEL = np.exp(-abs(bit_dist - bi_dist) / SIGMA)
            else:  # Custom SE × SOAP kernel (CUSTOM)
                KERNEL = (
                    np.exp((-((bit_dist - bi_dist) ** 2)) / (2 * SIGMA**2))
                    * (0.25 * (dpAAiti + dpBBiti + dpABiti + dpBAiti)) ** ZETA
                )

            # Here I already used ZETA before, as suggested by Prof. Gabor on 05/07/2019
            ICOHPs_TEST[tit] += alpha[i] * KERNEL

        SUMDIFF += (ICOHPs_TEST[tit] - float(BONDS_TEST[it].split()[5])) ** 2

        if DIR_TO_WRITE != "":

            ftw.write(
                "%d %d %10f %10f\n"
                % (tit, it, ICOHPs_TEST[tit], float(BONDS_TEST[it].split()[5]))
            )

            dtpx.append(ICOHPs_TEST[tit])

            if higher < ICOHPs_TEST[tit]:
                higher = ICOHPs_TEST[tit]

            dtpy.append(float(BONDS_TEST[it].split()[5]))
            if higher < float(BONDS_TEST[it].split()[5]):
                higher = float(BONDS_TEST[it].split()[5])

        tit += 1

    if DIR_TO_WRITE != "":
        # writting the root-mean square error ...
        RMSE = np.sqrt(SUMDIFF / len(ICOHPs_TEST))
        # ... and the variances of training and testing sets
        ftw.write(
            "TRAINING_VAR = %10f\nTRAINING_STD = %10f\nTESTING_VAR = %10f\nTESTING_STD = %10f\nRMSE = %10f\nMAX_ICOHP = %10f\n"
            % (VAR, STD, np.var(dtpy), np.std(dtpy), RMSE, higher)
        )
        ftw.close()

    return GenericStatusResponse(
        message=f" ... ",  # TODO: set message
        status=Status.DONE.value,
    )
