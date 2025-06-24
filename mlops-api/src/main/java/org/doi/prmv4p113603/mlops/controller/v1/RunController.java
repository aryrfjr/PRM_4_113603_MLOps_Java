package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.doi.prmv4p113603.mlops.data.dto.RunDto;
import org.doi.prmv4p113603.mlops.service.RunService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller querying of Run resources. For now, it supports only Read
 * operation and returns DTOs to ensure decoupling from internal models.
 */
@Tag(name = "CRUD")
@RestController
@RequestMapping("/api/v1/crud/runs")
@Validated
public class RunController {

    private final RunService service;

    public RunController(RunService service) {
        this.service = service;
    }

    /**
     * Lists all Runs filtered by (NominalComposition/Run) ID and ordered by Run ID.
     */
    @GetMapping
    @Operation(
            summary = "Lists all Runs filtered by (NominalComposition/Run) ID and ordered by Run ID.",
            description = "Lists all Runs filtered by (NominalComposition/Run) ID and ordered by Run ID."
    )
    public List<RunDto> handleRunsQuery(@RequestParam(required = false) Long nominalCompositionId,
                                        @RequestParam(required = false) Long runId) {
        // TODO: implement service for Run ID
        if (nominalCompositionId != null) {
            return service.listAllByNominalCompositionId(nominalCompositionId);
        } else {
            return null; // TODO: handle exception?
        }
    }

}
