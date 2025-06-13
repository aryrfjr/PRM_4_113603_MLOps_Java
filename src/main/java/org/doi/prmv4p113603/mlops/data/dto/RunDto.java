package org.doi.prmv4p113603.mlops.data.dto;

import lombok.*;

import java.util.List;

/**
 * This DTO representing a Run with all its attributes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunDto {

    /*
     * NOTE: This DTO wasn't split into a Create and Response because it is
     *  not directly exposed to or from client. In this specific case, this
     *  DTO is meant to help structure and pass data internally.
     */

    private Long nominalCompositionId;
    private int runNumber;
    private String status;
    private List<SubRunDto> subRuns;
}
