package org.doi.prmv4p113603.mlops.domain;

import org.doi.prmv4p113603.mlops.exception.SimulationDirectoryNotFoundException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SimulationArtifactPathParser {

    private static final Pattern RUN_PATH_PATTERN =
            Pattern.compile("/data/ML/big-data-full/([^/]+)/c/md/lammps/100/([^/]+)/");

    private static final Pattern SUB_RUN_PATH_PATTERN =
            Pattern.compile("/data/ML/big-data-full/([^/]+)/c/md/lammps/100/([^/]+)/2000/([^/]+)/");

    public SimulationArtifactPathInfo parse(String path) {

        Matcher subRunMatcher = SUB_RUN_PATH_PATTERN.matcher(path);
        if (subRunMatcher.find()) {
            return new SimulationArtifactPathInfo(
                    subRunMatcher.group(1),
                    subRunMatcher.group(2),
                    subRunMatcher.group(3)
            );
        }

        Matcher runMatcher = RUN_PATH_PATTERN.matcher(path);
        if (runMatcher.find()) {
            return new SimulationArtifactPathInfo(
                    runMatcher.group(1),
                    runMatcher.group(2),
                    null  // subRunNumber is not available
            );
        }

        throw new SimulationDirectoryNotFoundException(path);
    }

    /*
     * The keyword record is used to define a record class, which is a special kind of class
     * introduced in Java 14 (preview) and officially in Java 16. It is a concise way to create
     * immutable data carrier classes; i.e., classes that are just containers for data and
     * don't require much custom behavior.
     */
    public record SimulationArtifactPathInfo(String nominalCompositionName, String runNumber, String subRunNumber) {}

}
