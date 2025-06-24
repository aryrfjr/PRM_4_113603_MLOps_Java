package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class RunController {

    private final RunService service;

    /**
     * Retrieves a list with a set of Runs filtered by NominalComposition ID or
     * with a single Run by its ID.
     */
    @GetMapping
    @Operation(
            summary = "Retrieves a list with a set of Runs filtered by NominalComposition ID or with a single Run filtered by its ID.",
            description = "Retrieves a list with a set of Runs filtered by NominalComposition ID or with a single Run filtered by its ID."
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
