package org.doi.prmv4p113603.mlops.data.dto.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.doi.prmv4p113603.mlops.domain.MessageType;

/**
 * Describes the message structure.
 * <p>
 * TODO: could be the implementation of an interface MessageDto.
 */
@Data
public class AirflowKafkaMessageDto {

    private MessageType type;

    @JsonProperty("nominal_composition")
    private String nominalComposition;

    @JsonProperty("external_pipeline_run_id")
    private String externalPipelineRunId;

    private String timestamp;

}
