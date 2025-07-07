package org.doi.prmv4p113603.mlops.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ExplorationPipelineRunResponse {

    @JsonProperty("dag_id")
    private String dagId;

    @JsonProperty("dag_run_id")
    private String dagRunId;

    @JsonProperty("logical_date")
    private String logicalDate;

    private String state;

    private Map<String, Object> conf;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

}
