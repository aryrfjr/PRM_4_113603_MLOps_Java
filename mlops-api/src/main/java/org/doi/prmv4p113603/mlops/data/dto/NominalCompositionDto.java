package org.doi.prmv4p113603.mlops.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import lombok.*;

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

    private String name;

    private String description;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt = Instant.now();

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;

    private List<RunDto> runs;

    // Used in CRUD actions for NominalComposition entity
    public static NominalCompositionDto fromEntity(NominalComposition nc) {
        return NominalCompositionDto.builder()
                .id(nc.getId())
                .name(nc.getName())
                .description(nc.getDescription())
                .createdAt(nc.getCreatedAt())
                .updatedAt(nc.getUpdatedAt())
                .createdBy(nc.getCreatedBy())
                .updatedBy(nc.getUpdatedBy())
                .build();
    }

    public NominalComposition toEntity() {
        return NominalComposition.builder()
                .name(this.name)
                .description(this.description)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy("admin") // TODO: this is the only user for now
                .updatedBy("admin") // TODO: this is the only user for now
                .build();
    }

    // Used in the DataOps service when an exploration is requested
    public static NominalCompositionDto fromScheduleExploreExploitRequest(NominalComposition nc) {

        return NominalCompositionDto.builder()
                .name(nc.getName())
                .runs(nc.getRuns().stream()
                        .map(run -> RunDto.builder()
                                .runNumber(run.getRunNumber())
                                .status(run.getStatus())
                                .createdAt(run.getCreatedAt())
                                .completedAt(run.getCompletedAt())
                                .subRuns(
                                        run.getSubRuns().stream()
                                                .map(srun -> SubRunDto.builder()
                                                        .subRunNumber(srun.getSubRunNumber())
                                                        .status(srun.getStatus())
                                                        .scheduledAt(srun.getCreatedAt())
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
