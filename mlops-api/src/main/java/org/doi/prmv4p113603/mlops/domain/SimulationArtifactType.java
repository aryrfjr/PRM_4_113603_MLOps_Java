package org.doi.prmv4p113603.mlops.domain;

/**
 * Just a controlled vocabulary for simulation artifact type that will be persisted in the DB.
 */
public enum SimulationArtifactType {

    // LAMMPS
    LAMMPS_DUMP,
    LAMMPS_LOG,
    LAMMPS_INPUT,
    LAMMPS_OUTPUT,
    LAMMPS_DUMP_XYZ,

    // QE
    QE_SCF_IN,
    QE_SCF_OUT,

    // LOBSTER
    LOBSTER_INPUT,
    LOBSTER_INPUT_BND,
    LOBSTER_OUTPUT,
    LOBSTER_RUN_OUTPUT,
    ICOHPLIST,

    // SOAP
    SOAP_VECTORS;

}
