package org.doi.prmv4p113603.mlops.util;

import org.doi.prmv4p113603.mlops.domain.SimulationArtifactType;

import java.util.Map;

/**
 * Utility class with classification of simulation artifacts.
 */
public class ExpectedSimulationArtifacts {

    public static final Map<String, SimulationArtifactType> GENERATE_RUN_INPUTS = Map.of(
            "{NC}.lmp.inp", SimulationArtifactType.LAMMPS_INPUT
    );

    public static final Map<String, SimulationArtifactType> GENERATE_RUN_OUTPUTS = Map.of(
            "zca-th300.dump", SimulationArtifactType.LAMMPS_DUMP,
            "log.lammps", SimulationArtifactType.LAMMPS_LOG,
            "{NC}.lmp.out", SimulationArtifactType.LAMMPS_OUTPUT
    );

    public static final Map<String, SimulationArtifactType> GENERATE_SUB_RUN_INPUTS = Map.of(
            "{NC}.scf.in", SimulationArtifactType.QE_SCF_IN,
            "lobsterin", SimulationArtifactType.LOBSTER_INPUT,
            "lobsterin-quippy", SimulationArtifactType.LOBSTER_INPUT_BND,
            "{NC}.xyz", SimulationArtifactType.LAMMPS_DUMP_XYZ
    );

    public static final Map<String, SimulationArtifactType> GENERATE_SUB_RUN_OUTPUTS = Map.of(
            "{NC}.scf.out", SimulationArtifactType.QE_SCF_OUT,
            "{NC}.lb.out", SimulationArtifactType.LOBSTER_OUTPUT,
            "lobsterout", SimulationArtifactType.LOBSTER_RUN_OUTPUT,
            "ICOHPLIST.lobster", SimulationArtifactType.ICOHPLIST
    );

    public static final Map<String, SimulationArtifactType> ETL_INPUTS = Map.of(
            "{NC}.xyz", SimulationArtifactType.LAMMPS_DUMP_XYZ
    );

    public static final Map<String, SimulationArtifactType> ETL_OUTPUTS = Map.of(
            "SOAPS.vec", SimulationArtifactType.SOAP_VECTORS
    );

}
