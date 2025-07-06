package org.doi.prmv4p113603.mlops.data.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AirflowDagRunRequest {

    private Conf conf;

    @JsonProperty("dag_run_id")
    private String dagRunId;

    @JsonProperty("logical_date")
    private String logicalDate;

    private String note;

    public static AirflowDagRunRequest buildFrom(
            String nominalComposition,
            List<RunJob> runs,
            SoapParameters soap,
            String dagRunId,
            String logicalDate,
            String note
    ) {
        ExploreCellsTask task = ExploreCellsTask.builder()
                .nominalComposition(nominalComposition)
                .runsJobs(runs)
                .soapParameters(soap)
                .build();

        Conf conf = Conf.builder()
                .exploreCellsTask(task)
                .build();

        return AirflowDagRunRequest.builder()
                .conf(conf)
                .dagRunId(dagRunId)
                .logicalDate(logicalDate)
                .note(note)
                .build();
    }

    @Data
    @Builder
    public static class Conf {

        @JsonProperty("explore_cells_task")
        private ExploreCellsTask exploreCellsTask;

    }

    @Data
    @Builder
    public static class ExploreCellsTask {

        @JsonProperty("nominal_composition")
        private String nominalComposition;

        @JsonProperty("runs_jobs")
        private List<RunJob> runsJobs;

        @JsonProperty("soap_parameters")
        private SoapParameters soapParameters;

    }

    @Data
    @Builder
    public static class RunJob {

        @JsonProperty("run_number")
        private int runNumber;

        private List<Job> jobs;

    }

    @Data
    @Builder
    public static class Job {

        @JsonProperty("input_file")
        private String inputFile;

        @JsonProperty("output_files")
        private List<String> outputFiles;

    }

    @Data
    @Builder
    public static class SoapParameters {

        private double cutoff;

        @JsonProperty("l_max")
        private int lMax;

        @JsonProperty("n_max")
        private int nMax;

        @JsonProperty("n_Z")
        private int nZ;

        private String Z;

        @JsonProperty("n_species")
        private int nSpecies;

        @JsonProperty("Z_species")
        private String ZSpecies;

    }

    // Optional: for serialization
    public String toJson() throws Exception {
        return new ObjectMapper().writeValueAsString(this);
    }
}
