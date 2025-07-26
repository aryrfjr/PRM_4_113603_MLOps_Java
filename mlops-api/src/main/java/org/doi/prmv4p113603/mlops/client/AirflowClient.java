package org.doi.prmv4p113603.mlops.client;

import lombok.RequiredArgsConstructor;
import org.doi.prmv4p113603.mlops.data.request.AirflowDagRunRequest;
import org.doi.prmv4p113603.mlops.data.response.ExplorationPipelineRunResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AirflowClient {

    private final WebClient airflowWebClient;

    public ExplorationPipelineRunResponse triggerDag(String dagId, AirflowDagRunRequest payload) {
        return airflowWebClient.post()
                .uri("/dags/{dagId}/dagRuns", dagId)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ExplorationPipelineRunResponse.class)
                .block();
    }

    public ExplorationPipelineRunResponse getDagRunStatus(String dagId, String dagRunId) {
        return airflowWebClient.get()
                .uri("/dags/{dagId}/dagRuns/{dagRunId}", dagId, dagRunId)
                .retrieve()
                .bodyToMono(ExplorationPipelineRunResponse.class)
                .block();
    }
}
