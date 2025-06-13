package org.doi.prmv4p113603.mlops.data.dto;

import lombok.*;

import java.time.Instant;

/**
 * DTO representing a detected simulation artifact like files generated and
 * corresponding sizes or check-sums. It is associated to a SubRun.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationArtifactDto {

    /*
     * NOTE: This DTO wasn't split into a Create and Response because it is
     *  not directly exposed to or from client. In this specific case, this
     *  DTO is meant to help structure and pass data internally.
     */

    private String artifactType;
    private String filePath;
    private Long fileSize;
    private String checksum;
    private Instant createdAt;
}
