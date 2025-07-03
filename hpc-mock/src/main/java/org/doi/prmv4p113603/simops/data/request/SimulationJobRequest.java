package org.doi.prmv4p113603.simops.data.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SimulationJobRequest {

    @JsonProperty("input_file")
    private String inputFile;

    @JsonProperty("output_files")
    private List<String> outputFiles;

    @JsonProperty("depends_on_job_id")
    private Long dependsOnJobId;

}
