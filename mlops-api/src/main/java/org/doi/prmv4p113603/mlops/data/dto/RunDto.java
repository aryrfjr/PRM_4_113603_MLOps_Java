package org.doi.prmv4p113603.mlops.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.doi.prmv4p113603.mlops.domain.SimulationStatus;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;

import java.time.Instant;
import java.util.List;

/**
 * This DTO representing a Run with all its attributes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RunDto {

    /*
     * NOTE: In this project DTOs are reused for both internal use and external
     *  (API response) purposes. With the help of Jackson it is possible to control
     *  visibility to the client by setting fields to null or empty; and those fields
     *  will simply be excluded from the serialized JSON. This may require a refactoring
     *  regarding mixing internal and external concerns as the application grows.
     */

    private Long id;

    @JsonIgnore
    private NominalComposition nominalComposition;

    @JsonProperty("run_number")
    private int runNumber;

    private SimulationStatus status = SimulationStatus.SCHEDULED;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("started_at")
    private Instant startedAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("sub_runs")
    private List<SubRunDto> subRuns;

    // Used in CRUD actions for Run entity
    public static RunDto fromEntity(Run run) {
        return RunDto.builder()
                .id(run.getId())
                .runNumber(run.getRunNumber())
                .status(run.getStatus())
                .createdAt(run.getCreatedAt())
                .updatedAt(run.getUpdatedAt())
                .createdBy(run.getCreatedBy())
                .updatedBy(run.getUpdatedBy())
                .startedAt(run.getCompletedAt())
                .completedAt(run.getCompletedAt())
                .build();
    }

}
