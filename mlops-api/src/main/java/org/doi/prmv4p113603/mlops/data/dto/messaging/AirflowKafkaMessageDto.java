package org.doi.prmv4p113603.mlops.data.dto.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.doi.prmv4p113603.mlops.domain.MessageType;

import java.util.List;

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

    @JsonProperty("run_number")
    private int runNumber;

    @JsonProperty("sub_run_numbers")
    private List<Integer> subRunNumbers;

    @JsonProperty("runs_in_ssdb")
    private List<RunSubRunDto> runsInSsdb;

    @JsonProperty("external_pipeline_run_id")
    private String externalPipelineRunId;

    private String timestamp;

    /*
     * NOTE: In principle an inner class, to keep it scoped and private to this messageDTO.
     */
    @Data
    public static class RunSubRunDto {

        @JsonProperty("run_number")
        private int runNumber;

        @JsonProperty("sub_run_numbers")
        private List<Integer> subRunNumbers;
    }

}
