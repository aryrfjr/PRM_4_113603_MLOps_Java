package org.doi.prmv4p113603.mlops.testutil;

import org.doi.prmv4p113603.mlops.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Static methods that provide a fixed state of objects and data needed for testing.
 */
public class TestFixtures {


    /*
     This is a typical class used to establish a consistent environment for running tests.
     It ensures tests are repeatable by setting up preconditions and resources.
     */

    public static NominalComposition dummyNominalComposition(Long id, String name) {
        NominalComposition nc = new NominalComposition();
        nc.setId(id);
        nc.setName(name);
        return nc;
    }

    public static Run dummyRun(NominalComposition nc, int runNumber) {
        Run run = new Run();
        run.setNominalComposition(nc);
        run.setRunNumber(runNumber);
        run.setStatus("SCHEDULED");
        run.setCreatedAt(Instant.now());
        run.setUpdatedAt(Instant.now());
        run.setSubRuns(new ArrayList<>());
        return run;
    }

    public static SubRun dummySubRun(Run run, int subRunNumber) {
        SubRun sr = new SubRun();
        sr.setRun(run);
        sr.setSubRunNumber(subRunNumber);
        sr.setStatus("SCHEDULED");
        sr.setScheduledAt(Instant.now());
        sr.setSimulationArtifacts(new ArrayList<>());
        return sr;
    }

    public static SimulationArtifact dummyArtifact(SubRun subRun, String path, String type) {
        SimulationArtifact artifact = new SimulationArtifact();
        artifact.setSubRun(subRun);
        artifact.setArtifactType(type);
        artifact.setFilePath(path);
        artifact.setFileSize(1024);
        artifact.setChecksum("deadbeef1234567890");
        artifact.setCreatedAt(Instant.now());
        return artifact;
    }

    public static List<SimulationArtifact> dummyArtifactList(SubRun subRun, int count) {
        List<SimulationArtifact> artifacts = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            artifacts.add(dummyArtifact(subRun, "/fake/file_" + i + ".out", "OUTPUT"));
        }
        return artifacts;
    }
}
