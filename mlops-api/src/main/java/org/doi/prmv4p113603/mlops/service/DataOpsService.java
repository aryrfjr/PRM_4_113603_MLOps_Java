package org.doi.prmv4p113603.mlops.service;

import lombok.RequiredArgsConstructor;
import org.doi.prmv4p113603.mlops.client.AirflowClient;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.AirflowDagRunRequest;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.data.response.ExplorationPipelineRunResponse;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.exception.DataOpsInternalInconsistencyException;
import org.doi.prmv4p113603.mlops.model.ExplorationPipelineRun;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.model.SubRun;
import org.doi.prmv4p113603.mlops.repository.ExplorationPipelineRunRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataOpsService {

    private final RunService runService;
    private final SubRunService subRunService;
    private final SimulationDirectoryLoader directoryLoader;
    private final SimulationArtifactUploader uploader;
    private final AirflowClient airflowClient;
    private final AirflowPayloadBuilder payloadBuilder;
    private final ExplorationPipelineRunRepository pipelineRunRepo;

    @Transactional
    public NominalCompositionDto scheduleExploration(String compositionName, ScheduleExplorationRequest request) {
        NominalComposition composition = runService.loadCompositionOrThrow(compositionName);
        int nextRunNumber = runService.findNextRunNumber(composition);

        List<SimulationDirectory> runDirs = directoryLoader.loadExplorationDirs(
                compositionName, nextRunNumber, request.getNumSimulations());

        List<Run> runs = runService.createRunsFromDirectories(composition, runDirs);
        runService.saveAll(runs);

        NominalCompositionDto dto = NominalCompositionDto.fromScheduleExplorationRequest(composition, runs);
        uploader.uploadInputs(dto);

        AirflowDagRunRequest payload;
        try {
            payload = payloadBuilder.buildExplorationRequest(composition, runs);
        } catch (Exception e) {
            throw new DataOpsInternalInconsistencyException(e.getMessage());
        }

        ExplorationPipelineRunResponse response = airflowClient.triggerDag("pre_deployment_exploration", payload);

        ExplorationPipelineRun pipelineRun = ExplorationPipelineRun.builder()
                .responsePayload(response)
                .externalPipelineId(response.getPipelineId())
                .externalPipelineRunId(response.getPipelineRunId())
                .status(PipelineRunStatus.SCHEDULED)
                .runs(runs)
                .build();

        pipelineRunRepo.save(pipelineRun);
        return dto;
    }

    @Transactional
    public NominalCompositionDto scheduleExploitation(String compositionName, ScheduleExploitationRequest request) {
        NominalComposition composition = runService.loadCompositionOrThrow(compositionName);

        List<Long> runIds = request.getRuns().stream()
                .map(ScheduleExploitationRequest.RunInput::getId)
                .toList();

        /*
        List<Run> runs = runService.findAllByIds(runIds);
        validateRequestedRunIds(runIds, runs);
        subRunService.validateNoSubRunConflicts(request.getRuns());

        List<SimulationDirectory> runDirs = directoryLoader.loadExploitationDirs(compositionName, request.getRuns());
        List<SubRun> subRuns = subRunService.createSubRunsFromDirectories(compositionName, runs, runDirs);

        subRunService.saveAll(subRuns);

        NominalCompositionDto dto = NominalCompositionDto.fromScheduleExploitationRequest(composition, runs, subRuns);
        uploader.uploadInputs(dto);

        return dto;
        */

        return null;

    }

    private void validateRequestedRunIds(List<Long> requested, List<Run> found) {
        Set<Long> foundIds = found.stream().map(Run::getId).collect(Collectors.toSet());
        for (Long id : requested) {
            if (!foundIds.contains(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run with ID " + id + " not found");
            }
        }
    }

}
