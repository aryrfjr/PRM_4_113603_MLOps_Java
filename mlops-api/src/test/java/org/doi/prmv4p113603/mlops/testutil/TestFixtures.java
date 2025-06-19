package org.doi.prmv4p113603.mlops.testutil;

import org.doi.prmv4p113603.mlops.domain.SimulationStatus;
import org.doi.prmv4p113603.mlops.model.*;

import java.time.Instant;
import java.util.ArrayList;

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
        run.setStatus(SimulationStatus.SCHEDULED);
        run.setCreatedAt(Instant.now());
        run.setSubRuns(new ArrayList<>());
        return run;
    }

    public static SubRun dummySubRun(Run run, int subRunNumber) {
        SubRun sr = new SubRun();
        sr.setRun(run);
        sr.setSubRunNumber(subRunNumber);
        run.setStatus(SimulationStatus.SCHEDULED);
        run.setCreatedAt(Instant.now());
        sr.setSimulationArtifacts(new ArrayList<>());
        return sr;
    }

}
