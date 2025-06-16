package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.service.DataOpsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for DataOps.
 */
@Tag(name = "DataOps")
@RestController
@RequestMapping("/api/v1/dataops")
@Validated
public class DataOpsController {

    private final DataOpsService dataOpsService;

    public DataOpsController(DataOpsService dataOpsService) {
        this.dataOpsService = dataOpsService;
    }

    /**
     * Schedules exploration jobs for a given nominal composition by detecting local simulation artifacts.
     * <p>
     * This endpoint initiates a new Run and creates SubRuns and SimulationArtifacts for each valid folder found.
     * <p>
     * Example POST request:
     * POST /api/v1/dataops/generate/Zr49Cu49Al2
     * Body: { "numSimulations": 3 }
     *
     * @param nominalCompositionName the name of the nominal composition
     * @param request         payload indicating the number of sub-runs to process
     * @return created NominalCompositionDto with run, sub-run, and artifact information
     */
    @PostMapping("/generate/{nominal_composition}")
    public ResponseEntity<NominalCompositionDto> generateArtifacts(
            @PathVariable("nominal_composition") String nominalCompositionName,
            @Valid @RequestBody ScheduleExplorationRequest request
    ) {

        NominalCompositionDto result = dataOpsService.scheduleExploration(nominalCompositionName, request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);

    }
}
