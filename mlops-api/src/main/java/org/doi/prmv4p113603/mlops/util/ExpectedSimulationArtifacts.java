package org.doi.prmv4p113603.mlops.util;

import org.doi.prmv4p113603.mlops.domain.SimulationArtifactType;

import java.util.Map;

public class ExpectedSimulationArtifacts {

    public static final Map<String, SimulationArtifactType> RUN = Map.of(
            "zca-th300.dump", SimulationArtifactType.LAMMPS_DUMP,
            "log.lammps", SimulationArtifactType.LAMMPS_LOG,
            "{NC}.lmp.inp", SimulationArtifactType.LAMMPS_INPUT,
            "{NC}.lmp.out", SimulationArtifactType.LAMMPS_OUTPUT
    );

    public static final Map<String, SimulationArtifactType> SUB_RUN = Map.of(
            "{NC}.scf.in", SimulationArtifactType.QE_SCF_IN,
            "{NC}.xyz", SimulationArtifactType.LAMMPS_DUMP_XYZ,
            "lobsterin", SimulationArtifactType.LOBSTER_INPUT,
            "SOAPS.vec", SimulationArtifactType.SOAP_VECTORS,
            "ICOHPLIST.lobster", SimulationArtifactType.ICOHPLIST
    );

}
