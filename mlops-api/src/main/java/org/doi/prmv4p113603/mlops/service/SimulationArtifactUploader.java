package org.doi.prmv4p113603.mlops.service;

import lombok.RequiredArgsConstructor;
import org.doi.prmv4p113603.common.service.MinioStorageService;
import org.doi.prmv4p113603.common.util.MinioUtils;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.dto.SimulationArtifactDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulationArtifactUploader {

    private final MinioStorageService minioStorageService;

    public void uploadInputs(NominalCompositionDto dto) {
        dto.getRuns().stream()
                .flatMap(runDto -> runDto.getSubRuns().stream())
                .flatMap(subRunDto -> subRunDto.getSimulationArtifacts().stream())
                .filter(artifactDto -> artifactDto.getArtifactRole().isGenerateInput())
                .map(SimulationArtifactDto::getFilePath)
                .forEach(path -> minioStorageService.uploadFile(MinioUtils.pathToKey(path), path));
    }
}
