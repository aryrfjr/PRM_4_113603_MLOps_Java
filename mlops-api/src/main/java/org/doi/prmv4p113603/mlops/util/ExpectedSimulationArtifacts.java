package org.doi.prmv4p113603.mlops.util;

import org.doi.prmv4p113603.mlops.domain.SimulationArtifactType;

import java.util.Map;

/**
 * Utility class with classification of simulation artifacts.
 */
public class ExpectedSimulationArtifacts {

    public static final Map<String, SimulationArtifactType> RUN_INPUTS = Map.of(
            "{NC}.lmp.inp", SimulationArtifactType.LAMMPS_INPUT
    );

    public static final Map<String, SimulationArtifactType> RUN_OUTPUTS = Map.of(
            "zca-th300.dump", SimulationArtifactType.LAMMPS_DUMP,
            "log.lammps", SimulationArtifactType.LAMMPS_LOG,
            "{NC}.lmp.out", SimulationArtifactType.LAMMPS_OUTPUT
    );

    public static final Map<String, SimulationArtifactType> SUB_RUN_INPUTS = Map.of(
            "{NC}.scf.in", SimulationArtifactType.QE_SCF_IN,
            "lobsterin", SimulationArtifactType.LOBSTER_INPUT
    );

    public static final Map<String, SimulationArtifactType> SUB_RUN_OUTPUTS = Map.of(
            "{NC}.xyz", SimulationArtifactType.LAMMPS_DUMP_XYZ,
            "SOAPS.vec", SimulationArtifactType.SOAP_VECTORS,
            "ICOHPLIST.lobster", SimulationArtifactType.ICOHPLIST
    );

}
