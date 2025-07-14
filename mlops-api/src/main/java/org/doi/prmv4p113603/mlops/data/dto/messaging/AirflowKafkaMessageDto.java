package org.doi.prmv4p113603.mlops.data.dto.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.doi.prmv4p113603.mlops.domain.MessageType;

import java.time.OffsetDateTime;
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

    @JsonProperty("job_info")
    private HpcJobInfoDto jobInfo;

    @JsonProperty("external_pipeline_run_id")
    private String externalPipelineRunId;

    private String timestamp;

    /*
     * NOTE: In principle an inner classes, to keep them scoped and private to this messageDTO.
     */

    @Data
    public static class RunSubRunDto {

        @JsonProperty("run_number")
        private int runNumber;

        @JsonProperty("sub_run_numbers")
        private List<Integer> subRunNumbers;
    }

    @Data
    public class HpcJobInfoDto {

        private int id;

        private String status;

        @JsonProperty("input_file")
        private String inputFile;

        @JsonProperty("output_files")
        private List<String> outputFiles;

        @JsonProperty("submitted_at")
        private OffsetDateTime submittedAt;

        @JsonProperty("started_at")
        private OffsetDateTime startedAt;

        @JsonProperty("completed_at")
        private OffsetDateTime completedAt;

        @JsonProperty("depends_on_job_id")
        private int dependsOnJobId;

    }

}
