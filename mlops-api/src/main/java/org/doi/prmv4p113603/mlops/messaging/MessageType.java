package org.doi.prmv4p113603.mlops.messaging;

/**
 * Represents the execution state pipelines for Pre-Deployment Exploration/Exploitation pipelines.
 */
public enum MessageType {

    RUN_SUBMITTED,
    RUN_SUBMISSION_FAILED,
    RUN_JOB_FINISHED,
    RUN_JOB_FAILED,
    SOAP_VECTORS_EXTRACTED,
    SOAP_VECTORS_EXTRACTION_FAILED,
    SSDB_CREATED,
    SSDB_CREATION_FAILED;

    // Workflow orchestration tool has submitted all its simulations (including sub-Runs) to the HPC service
    public boolean isRunSubmitted() {
        return this == RUN_SUBMITTED;
    }

    public boolean isRunSubmissionFailed() {
        return this == RUN_SUBMISSION_FAILED;
    }

    // Simulation submitted by Workflow orchestration tool to the HPC service finished or failed
    public boolean isRunJobFinished() {
        return this == RUN_JOB_FINISHED;
    }

    public boolean isRunJobFailed() {
        return this == RUN_JOB_FAILED;
    }

    // Workflow orchestration tool has created all the SOAPS.vec files for its sub0runs
    public boolean isSoapVectorsExtracted() {
        return this == SOAP_VECTORS_EXTRACTED;
    }

    public boolean isSoapVectorsExtractionFailed() {
        return this == SOAP_VECTORS_EXTRACTION_FAILED;
    }

    // Workflow orchestration tool has created its SSDB with all current sub-Runs
    public boolean isSsdbCreated() {
        return this == SSDB_CREATED;
    }

    public boolean isSsdbCreationFailed() {
        return this == SSDB_CREATION_FAILED;
    }

}
