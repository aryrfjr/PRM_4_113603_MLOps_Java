package org.doi.prmv4p113603.mlops.testutil;

import org.doi.prmv4p113603.mlops.domain.RunStatus;
import org.doi.prmv4p113603.mlops.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Static methods that provide a fixed state of objects and data needed for testing.
 */
/*
 * NOTE: This is a typical class used to establish a consistent environment for running tests.
 *  It ensures tests are repeatable by setting up preconditions and resources.
 */
public class TestFixtures {

    /*
     * NOTE: Despite Verbosity at call site, and prone to performance overhead, using Optional
     *  for "optional" arguments is acceptable within this test context; however, it is important
     *  to stress that Optional is not intended for parameters (in production), it's intended
     *  for return values.
     */

    public static NominalComposition dummyNominalComposition(Optional<Long> id, String name) {

        NominalComposition.NominalCompositionBuilder builder = NominalComposition.builder()
                .name(name)
                .description(name + " test")
                .createdBy("test")
                .createdAt(Instant.now());

        id.ifPresent(builder::id);

        return builder.build();

    }

    public static Run dummyRun(Optional<Long> id,
                               NominalComposition nominalComposition,
                               int number,
                               RunStatus runStatus,
                               Optional<List<SubRun>> subRuns) {

        Run.RunBuilder builder = Run.builder()
                .nominalComposition(nominalComposition)
                .runNumber(number)
                .status(runStatus)
                .createdBy("test")
                .createdAt(Instant.now());

        id.ifPresent(builder::id);
        subRuns.ifPresent(builder::subRuns);

        return builder.build();

    }

    public static SubRun dummySubRun(Optional<Long> id,
                                     Run run,
                                     int number,
                                     Optional<List<SimulationArtifact>> simulationArtifacts) {

        SubRun.SubRunBuilder builder = SubRun.builder()
                .run(run)
                .subRunNumber(number)
                .createdBy("test")
                .createdAt(Instant.now());

        id.ifPresent(builder::id);
        simulationArtifacts.ifPresent(builder::simulationArtifacts);

        return builder.build();

    }

}
