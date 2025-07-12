package org.doi.prmv4p113603.mlops.util;

import org.doi.prmv4p113603.mlops.domain.SimulationArtifactType;

import java.util.*;

/**
 * Utility class with classification of simulation artifacts.
 */
public class ExpectedSimulationArtifacts {

    // Central storage of all mappings grouped by category
    private static final Map<String, Map<String, SimulationArtifactType>> ALL_MAPPINGS = Map.of(
            "GENERATE_RUN_INPUTS", Map.of(
                    "{NC}.lmp.inp", SimulationArtifactType.LAMMPS_INPUT
            ),
            "GENERATE_RUN_OUTPUTS", Map.of(
                    "zca-th300.dump", SimulationArtifactType.LAMMPS_DUMP,
                    "log.lammps", SimulationArtifactType.LAMMPS_LOG,
                    "{NC}.lmp.out", SimulationArtifactType.LAMMPS_OUTPUT
            ),
            "GENERATE_SUB_RUN_INPUTS", Map.of(
                    "{NC}.scf.in", SimulationArtifactType.QE_SCF_IN,
                    "lobsterin", SimulationArtifactType.LOBSTER_INPUT,
                    "lobsterin-quippy", SimulationArtifactType.LOBSTER_INPUT_BND,
                    "{NC}.xyz", SimulationArtifactType.LAMMPS_DUMP_XYZ
            ),
            "GENERATE_SUB_RUN_OUTPUTS", Map.of(
                    "{NC}.scf.out", SimulationArtifactType.QE_SCF_OUT,
                    "{NC}.lb.out", SimulationArtifactType.LOBSTER_OUTPUT,
                    "lobsterout", SimulationArtifactType.LOBSTER_RUN_OUTPUT,
                    "ICOHPLIST.lobster", SimulationArtifactType.ICOHPLIST
            ),
            "ETL_INPUTS", Map.of(
                    "{NC}.xyz", SimulationArtifactType.LAMMPS_DUMP_XYZ
            ),
            "ETL_OUTPUTS", Map.of(
                    "SOAPS.vec", SimulationArtifactType.SOAP_VECTORS
            )
    );

    // Re-expose each category for compatibility
    public static final Map<String, SimulationArtifactType> GENERATE_RUN_INPUTS = ALL_MAPPINGS.get("GENERATE_RUN_INPUTS");
    public static final Map<String, SimulationArtifactType> GENERATE_RUN_OUTPUTS = ALL_MAPPINGS.get("GENERATE_RUN_OUTPUTS");
    public static final Map<String, SimulationArtifactType> GENERATE_SUB_RUN_INPUTS = ALL_MAPPINGS.get("GENERATE_SUB_RUN_INPUTS");
    public static final Map<String, SimulationArtifactType> GENERATE_SUB_RUN_OUTPUTS = ALL_MAPPINGS.get("GENERATE_SUB_RUN_OUTPUTS");
    public static final Map<String, SimulationArtifactType> ETL_INPUTS = ALL_MAPPINGS.get("ETL_INPUTS");
    public static final Map<String, SimulationArtifactType> ETL_OUTPUTS = ALL_MAPPINGS.get("ETL_OUTPUTS");

    /**
     * Returns the filename pattern (e.g., "SOAPS.vec") for the given SimulationArtifactType.
     *
     * @param type SimulationArtifactType to search for
     * @return the first matching filename pattern, or null if not found
     */
    public static String getFileNamePatternForType(SimulationArtifactType type) {
        return ALL_MAPPINGS.values().stream()
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> entry.getValue() == type)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

}
