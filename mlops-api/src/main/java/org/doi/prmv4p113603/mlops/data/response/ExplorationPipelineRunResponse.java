package org.doi.prmv4p113603.mlops.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ExplorationPipelineRunResponse {

    @JsonProperty("dag_id") // TODO: I think that DAG is Airflow specific; how to generalize?
    private String pipelineId;

    @JsonProperty("dag_run_id") // TODO: I think that DAG is Airflow specific; how to generalize?
    private String pipelineRunId;

    /*
     * NOTE: In Airflow, the logical date is a timestamp that represents the data interval
     *  a DAG run is responsible for processing; not the time the DAG actually started.
     *  Basically, it represents the day of data being processed, not when it was launched.
     */
    @JsonProperty("logical_date") // TODO: I think that DAG is Airflow specific; how to generalize?
    private String logicalDate;

    private String state;

    private Map<String, Object> conf;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

}
