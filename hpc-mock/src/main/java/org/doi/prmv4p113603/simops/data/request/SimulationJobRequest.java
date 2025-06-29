package org.doi.prmv4p113603.simops.data.request;

import lombok.*;

import java.util.List;

/**
 * Request payload for job scheduling.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationJobRequest {

    private String inputFile;
    private List<String> outputFiles;
    private Long dependsOnJobId; // TODO: optional

}
