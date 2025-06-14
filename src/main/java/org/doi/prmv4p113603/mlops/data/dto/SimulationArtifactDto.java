package org.doi.prmv4p113603.mlops.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.doi.prmv4p113603.mlops.model.SubRun;
import java.time.Instant;

/**
 * DTO representing a detected simulation artifact like files generated and
 * corresponding sizes or check-sums. It is associated to a SubRun.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SimulationArtifactDto {

    /*
     * NOTE: In this project DTOs are reused for both internal use and external
     *  (API response) purposes. With the help of Jackson it is possible to control
     *  visibility to the client by setting fields to null or empty; and those fields
     *  will simply be excluded from the serialized JSON. This may require a refactoring
     *  regarding mixing internal and external concerns as the application grows.
     */

    private Long id;

    @JsonIgnore
    private SubRun subRun;

    @JsonProperty("artifact_type")
    private String artifactType;

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("file_size")
    private Integer fileSize;

    private String checksum; // TODO: Index this column for lookup; use it to detect duplicate artifacts

    @JsonProperty("created_at")
    private Instant createdAt = Instant.now();

}
