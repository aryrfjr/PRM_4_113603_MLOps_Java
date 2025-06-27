package org.doi.prmv4p113603.mlops.util;

import jakarta.xml.bind.DatatypeConverter;
import org.doi.prmv4p113603.mlops.domain.SimulationArtifactRole;
import org.doi.prmv4p113603.mlops.domain.SimulationArtifactType;
import org.doi.prmv4p113603.mlops.domain.SimulationDirectory;
import org.doi.prmv4p113603.mlops.exception.SimulationArtifactNotFoundException;
import org.doi.prmv4p113603.mlops.model.SimulationArtifact;
import org.doi.prmv4p113603.mlops.model.SubRun;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class that creates instances of entity class SimulationArtifact.
 */
public class SimulationArtifactFactory {

    private SimulationArtifactFactory() {
        /*
         * Prevent instantiation. Making the constructor private and the class final is
         * a common best practice for utility classes.
         */
    }

    public static List<SimulationArtifact> load(String nominalCompositionName,
                                                SubRun subRun,
                                                SimulationDirectory subRunDir,
                                                SimulationArtifactRole role) {

        List<SimulationArtifact> loaded = new ArrayList<>();

        // SubRun 0 is the reference structure and also contains the Runs inputs/outputs as artifacts
        if (subRun.getSubRunNumber() == 0) {

            if (role == SimulationArtifactRole.INPUT) {
                loaded.addAll(loadExpectedSimulationArtifacts(
                        ExpectedSimulationArtifacts.RUN_INPUTS,
                        nominalCompositionName,
                        subRun,
                        subRunDir.getParent().getPath(),
                        SimulationArtifactRole.INPUT));
            } else if (role == SimulationArtifactRole.OUTPUT) {
                loaded.addAll(loadExpectedSimulationArtifacts(
                        ExpectedSimulationArtifacts.RUN_OUTPUTS,
                        nominalCompositionName,
                        subRun,
                        subRunDir.getParent().getPath(),
                        SimulationArtifactRole.OUTPUT));
            }

        }

        if (role == SimulationArtifactRole.INPUT) {
            loaded.addAll(loadExpectedSimulationArtifacts(
                    ExpectedSimulationArtifacts.SUB_RUN_INPUTS,
                    nominalCompositionName,
                    subRun,
                    subRunDir.getPath(),
                    SimulationArtifactRole.INPUT));
        } else if (role == SimulationArtifactRole.OUTPUT) {
            loaded.addAll(loadExpectedSimulationArtifacts(
                    ExpectedSimulationArtifacts.SUB_RUN_OUTPUTS,
                    nominalCompositionName,
                    subRun,
                    subRunDir.getPath(),
                    SimulationArtifactRole.OUTPUT));
        }

        return loaded;

    }

    /*
     * Helpers
     */

    private static List<SimulationArtifact> loadExpectedSimulationArtifacts(
            Map<String, SimulationArtifactType> expectedSimulationArtifacts,
            String nominalCompositionName,
            SubRun subRun, String path, SimulationArtifactRole role) {

        return expectedSimulationArtifacts.entrySet().stream()
                .map(entry -> {

                    String template = entry.getKey();
                    SimulationArtifactType type = entry.getValue();

                    String resolvedFileName = template.replace("{NC}", nominalCompositionName);
                    Path subRunDirPath;

                    if (type == SimulationArtifactType.SOAP_VECTORS) {
                        subRunDirPath = Path.of(path.replace(
                                nominalCompositionName, nominalCompositionName + "-SOAPS"));
                    } else {
                        subRunDirPath = Path.of(path);
                    }

                    Path resolvedPath = subRunDirPath.resolve(resolvedFileName);

                    if (!Files.exists(resolvedPath)) {
                        throw new SimulationArtifactNotFoundException(resolvedPath.toAbsolutePath().toString());
                    }

                    return buildArtifact(resolvedPath, subRun, type, role);

                })
                .filter(Objects::nonNull) // Remove nulls (i.e., missing files)
                .toList();

    }

    private static SimulationArtifact buildArtifact(Path filePath,
                                                    SubRun subRun,
                                                    SimulationArtifactType type,
                                                    SimulationArtifactRole role) {

        return SimulationArtifact.builder()
                .subRun(subRun)
                .artifactType(type)
                .artifactRole(role)
                .filePath(filePath.toString())
                .fileSize(getFileSize(filePath))
                .checksum(computeChecksum(filePath))
                .build();

    }

    private static Integer getFileSize(Path filePath) {

        try {
            return (int) Files.size(filePath);
        } catch (IOException e) {
            return null;
        }

    }

    private static String computeChecksum(Path filePath) {

        try (InputStream is = Files.newInputStream(filePath)) {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(is.readAllBytes());
            return DatatypeConverter.printHexBinary(hash).toLowerCase();

        } catch (Exception e) {
            return null;
        }

    }

}
