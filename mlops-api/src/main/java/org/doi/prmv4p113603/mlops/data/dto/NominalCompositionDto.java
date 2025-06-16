package org.doi.prmv4p113603.mlops.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import lombok.*;
import org.doi.prmv4p113603.mlops.model.Run;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO used to send NominalComposition data back to the client. It
 * includes ID and timestamps, reflecting data persisted in the database.
 *
 * @see org.doi.prmv4p113603.mlops.model.NominalComposition
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NominalCompositionDto {


    /*
     * NOTE: In this project DTOs are reused for both internal use and external
     *  (API response) purposes. With the help of Jackson it is possible to control
     *  visibility to the client by setting fields to null or empty; and those fields
     *  will simply be excluded from the serialized JSON. This may require a refactoring
     *  regarding mixing internal and external concerns as the application grows.
     */

    private Long id;

    @JsonProperty("nominal_composition_name")
    private String name;

    private String description;

    @JsonProperty("created_at")
    private Instant createdAt;

    private List<RunDto> runs;

    // Used in CRUD actions for NominalComposition entity
    public static NominalCompositionDto fromEntity(NominalComposition nc) {
        return NominalCompositionDto.builder()
                .id(nc.getId())
                .name(nc.getName())
                .description(nc.getDescription())
                .createdAt(nc.getCreatedAt())
                .build();
    }

    public NominalComposition toEntity() {
        return NominalComposition.builder()
                .name(this.name)
                .description(this.description)
                .createdAt(Instant.now())
                .build();
    }

    // Used in the DataOps service when an exploration is requested
    public static NominalCompositionDto fromScheduleExploreExploitRequest(NominalComposition nc, List<Run> runs) {

        return NominalCompositionDto.builder()
                .name(nc.getName())
                .runs(runs.stream()
                        .map(run -> RunDto.builder()
                                .runNumber(run.getRunNumber())
                                .status(run.getStatus())
                                .createdAt(run.getCreatedAt())
                                .updatedAt(run.getUpdatedAt())
                                .subRuns(
                                        run.getSubRuns().stream()
                                                .map(srun -> SubRunDto.builder()
                                                        .subRunNumber(srun.getSubRunNumber())
                                                        .status(srun.getStatus())
                                                        .scheduledAt(srun.getScheduledAt())
                                                        .completedAt(srun.getCompletedAt())
                                                        .simulationArtifacts(
                                                                srun.getSimulationArtifacts().stream()
                                                                        .map(sas -> SimulationArtifactDto.builder()
                                                                                .artifactType(sas.getArtifactType())
                                                                                .filePath(sas.getFilePath())
                                                                                .fileSize(sas.getFileSize())
                                                                                .createdAt(sas.getCreatedAt())
                                                                                .build())
                                                                        .collect(Collectors.toList()))
                                                        .build())
                                                .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();

    }

}
