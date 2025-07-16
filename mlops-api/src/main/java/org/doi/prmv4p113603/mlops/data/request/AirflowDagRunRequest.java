package org.doi.prmv4p113603.mlops.data.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AirflowDagRunRequest { // TODO: I think that DAG is Airflow specific; how to generalize?

    private Conf conf;
    private String note;

    public static AirflowDagRunRequest buildFrom(
            String nominalComposition,
            List<RunJob> runsJobs,
            List<RunWithSubRuns> allRunsWithSubRuns,
            SoapParameters soap,
            String note
    ) {
        ExploreCellsTask task = ExploreCellsTask.builder()
                .nominalComposition(nominalComposition)
                .runsJobs(runsJobs)
                .allRunsWithSubRuns(allRunsWithSubRuns)
                .soapParameters(soap)
                .build();

        Conf conf = Conf.builder()
                .exploreCellsTask(task)
                .build();

        return AirflowDagRunRequest.builder()
                .conf(conf)
                .note(note)
                .build();
    }

    /*
     * NOTE: In principle an inner classes, to keep them scoped and private to this messageDTO.
     */

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

        @JsonProperty("all_runs_with_sub_runs")
        private List<RunWithSubRuns> allRunsWithSubRuns;

        @JsonProperty("runs_jobs")
        private List<RunJob> runsJobs;

        @JsonProperty("soap_parameters")
        private SoapParameters soapParameters;

    }

    @Data
    @Builder
    public static class RunWithSubRuns {

        @JsonProperty("run_number")
        private int runNumber;

        @JsonProperty("sub_run_numbers")
        private List<Integer> subRunsNumbers;

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

        @JsonProperty("Z")
        private String z;

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
