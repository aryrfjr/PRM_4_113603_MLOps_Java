package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.SimulationArtifactDto;
import org.doi.prmv4p113603.mlops.service.SimulationArtifactService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller querying of SimulationArtifact resources. For now, it supports only Read
 * operation and returns DTOs to ensure decoupling from internal models.
 */
@Tag(name = "CRUD")
@RestController
@RequestMapping("/api/v1/crud/simulation_artifacts")
@Validated
@AllArgsConstructor
public class SimulationArtifactController {

    private final SimulationArtifactService service;

    /**
     * Retrieves a list with a set of SimulationArtifact entities filtered by SubRun ID or with a single SimulationArtifact filtered by its ID.
     */
    @GetMapping
    @Operation(
            summary = "Retrieves a list with a set of SimulationArtifact entities filtered by SubRun ID or with a single SimulationArtifact filtered by its ID.",
            description = "Retrieves a list with a set of SimulationArtifact entities filtered by SubRun ID or with a single SimulationArtifact filtered by its ID."
    )
    public List<SimulationArtifactDto> handleSimulationArtifactsQuery(@RequestParam(required = false) Long subRunId,
                                                                      @RequestParam(required = false) Long simulationArtifactId) {
        // TODO: implement service for SimulationArtifact ID
        if (subRunId != null) {
            return service.listAllBySubRunId(subRunId);
        } else {
            return null; // TODO: handle exception?
        }
    }

}
