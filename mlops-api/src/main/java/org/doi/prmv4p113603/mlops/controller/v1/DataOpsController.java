package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExploitationRequest;
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
@AllArgsConstructor
public class DataOpsController {

    private final DataOpsService dataOpsService;

    /**
     * Schedules configuration space exploration for a given nominal composition.
     * <p>
     * This endpoint initiates a new Run and creates SubRuns and SimulationArtifacts for each valid folder found.
     * <p>
     * Example POST request:
     * POST /api/v1/dataops/generate/Zr49Cu49Al2
     * Body:
     * <pre>
     * {
     *   "numSimulations": 3 // TODO: num_simulations
     * }
     * </pre>
     *
     * @param nominalCompositionName the name of the nominal composition
     * @param request                payload indicating the number of sub-runs to process
     * @return created NominalCompositionDto with run, sub-run, and artifact information
     */
    @PostMapping("/generate/{nominal_composition}")
    public ResponseEntity<NominalCompositionDto> scheduleExploration(
            @PathVariable("nominal_composition") String nominalCompositionName,
            @Valid @RequestBody ScheduleExplorationRequest request
    ) {

        NominalCompositionDto result = dataOpsService.scheduleExploration(nominalCompositionName, request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);

    }

    /**
     * Schedules geometry augmentation (exploitation) for a given nominal composition and runs.
     * <p>
     * This endpoint creates SubRuns and SimulationArtifacts for each valid folder found.
     * <p>
     * Example POST request:
     * POST /api/v1/dataops/generate/Zr49Cu49Al2/augment
     * Body:
     * <pre>
     * { "runs": [
     *   {
     *     "id": 1,
     *     "run_number": 1,
     *     "sub_runs": [1,2,3,4,5]
     *   },
     *   {
     *     "id": 2,
     *     "run_number": 2,
     *     "sub_runs": [6,7,13,14]
     *   }
     * ]}
     * </pre>
     *
     * @param nominalCompositionName the name of the nominal composition
     * @param request                payload indicating the set of runs to process
     * @return updated NominalCompositionDto with run, sub-run, and artifact information
     */
    @PostMapping("/generate/{nominal_composition}/augment")
    public ResponseEntity<NominalCompositionDto> scheduleExploitation(
            @PathVariable("nominal_composition") String nominalCompositionName,
            @Valid @RequestBody ScheduleExploitationRequest request
    ) {

        NominalCompositionDto result = dataOpsService.scheduleExploitation(nominalCompositionName, request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);

    }

}
