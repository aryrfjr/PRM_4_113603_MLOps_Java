package org.doi.prmv4p113603.mlops.util;

import jakarta.xml.bind.DatatypeConverter;
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
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static List<SimulationArtifact> load(String nominalCompositionName, SubRun subRun, SimulationDirectory subRunDir) {

        return ExpectedSimulationArtifacts.SUB_RUN.entrySet().stream()
                .map(entry -> {

                    String template = entry.getKey();
                    SimulationArtifactType type = entry.getValue();

                    String resolvedFileName = template.replace("{NC}", nominalCompositionName);
                    Path subRunDirPath;

                    if (type == SimulationArtifactType.SOAP_VECTORS) {
                        subRunDirPath = Path.of(subRunDir.getPath().replace(
                                nominalCompositionName, nominalCompositionName + "-SOAPS"));
                    } else {
                        subRunDirPath = Path.of(subRunDir.getPath());
                    }

                    Path resolvedPath = subRunDirPath.resolve(resolvedFileName);

                    if (!Files.exists(resolvedPath)) {
                        throw new SimulationArtifactNotFoundException("Simulation artifact file '" +
                                resolvedPath.toAbsolutePath() + "',  not found");
                    }

                    return buildArtifact(resolvedPath, subRun, type);

                })
                .filter(Objects::nonNull) // Remove nulls (i.e., missing files)
                .collect(Collectors.toList());

    }

    /*
     * Helpers
     */

    private static SimulationArtifact buildArtifact(Path filePath, SubRun subRun, SimulationArtifactType type) {

        return SimulationArtifact.builder()
                .subRun(subRun)
                .artifactType(type)
                .filePath(filePath.toString())
                .fileSize(getFileSize(filePath))
                .checksum(computeChecksum(filePath))
                .createdAt(Instant.now())
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
