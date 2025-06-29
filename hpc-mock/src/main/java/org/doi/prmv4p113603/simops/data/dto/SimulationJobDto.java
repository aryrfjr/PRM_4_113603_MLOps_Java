package org.doi.prmv4p113603.simops.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doi.prmv4p113603.simops.domain.SimulationJobStatus;
import org.doi.prmv4p113603.simops.model.SimulationJob;

import java.time.Instant;
import java.util.List;

/**
 * DTO used to send SimulationJob data back to the client.
 *
 * @see org.doi.prmv4p113603.simops.model.SimulationJob
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SimulationJobDto {

    private Long id;

    @JsonProperty("input_file")
    private String inputFile;

    @JsonProperty("input_files")
    private List<String> outputFiles;

    private SimulationJobStatus status;

    @JsonProperty("submitted_at")
    private Instant submittedAt;

    @JsonProperty("started_at")
    private Instant startedAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("dependsOn_id")
    private Long dependsOnId;

    public static SimulationJobDto fromEntity(SimulationJob job) {
        return new SimulationJobDto(
                job.getId(),
                job.getInputFile(),
                job.getOutputFiles(),
                job.getStatus(),
                job.getSubmittedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getDependsOn() != null ? job.getDependsOn().getId() : null
        );
    }

}
