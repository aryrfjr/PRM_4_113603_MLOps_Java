package org.doi.prmv4p113603.mlops.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.doi.prmv4p113603.mlops.model.SubRun;

import java.time.Instant;
import java.util.List;

/**
 * This DTO representing a SubRun with all its attributes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SubRunDto {

    /*
     * NOTE: In this project DTOs are reused for both internal use and external
     *  (API response) purposes. With the help of Jackson it is possible to control
     *  visibility to the client by setting fields to null or empty; and those fields
     *  will simply be excluded from the serialized JSON. This may require a refactoring
     *  regarding mixing internal and external concerns as the application grows.
     */

    private Long id;

    @JsonProperty("sub_run_number")
    private int subRunNumber;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("external_pipeline_run_id")
    private String externalPipelineRunId;

    @JsonProperty("simulation_artifacts")
    private List<SimulationArtifactDto> simulationArtifacts;

    /*
     * TODO: List<BondInteractionDto> bonds; List<DescriptorFileDto> descriptorFiles;
     */

    // Used in CRUD actions for SubRun entity
    public static SubRunDto fromEntity(SubRun subRun) {
        return SubRunDto.builder()
                .id(subRun.getId())
                .subRunNumber(subRun.getSubRunNumber())
                .createdAt(subRun.getCreatedAt())
                .updatedAt(subRun.getUpdatedAt())
                .createdBy(subRun.getCreatedBy())
                .updatedBy(subRun.getUpdatedBy())
                .externalPipelineRunId(subRun.getExternalPipelineRunId())
                .build();
    }

}
