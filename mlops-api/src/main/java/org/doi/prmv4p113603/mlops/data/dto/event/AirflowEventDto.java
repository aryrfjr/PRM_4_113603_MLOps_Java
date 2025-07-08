package org.doi.prmv4p113603.mlops.data.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Describes the message structure.
 */
@Data
public class AirflowEventDto {

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("nominal_composition")
    private String nominalComposition;

    @JsonProperty("external_pipeline_run_id")
    private String externalPipelineRunId;

    private String timestamp;

}
