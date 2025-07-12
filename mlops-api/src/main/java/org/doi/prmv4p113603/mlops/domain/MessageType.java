package org.doi.prmv4p113603.mlops.domain;

/**
 * Represents the execution state pipelines for Pre-Deployment Exploration/Exploitation pipelines.
 */
public enum MessageType {
    SSDB_CREATED,
    SOAP_VECTORS_EXTRACTED;

    public boolean isSsdbCreated() {
        return this == SSDB_CREATED;
    }

    public boolean isSoapVectorsExtracted() {
        return this == SOAP_VECTORS_EXTRACTED;
    }

}
