package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.SubRunDto;
import org.doi.prmv4p113603.mlops.service.SubRunService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller querying of SubRun resources. For now, it supports only Read
 * operation and returns DTOs to ensure decoupling from internal models.
 */
@Tag(name = "CRUD")
@RestController
@RequestMapping("/api/v1/crud/sub_runs")
@Validated
@AllArgsConstructor
public class SubRunController {

    private final SubRunService service;

    /**
     * Retrieves a list with a set of SubRuns filtered by Run ID or with a single SubRun filtered by its ID.
     */
    @GetMapping
    @Operation(
            summary = "Retrieves a list with a set of SubRuns filtered by Run ID or with a single SubRun filtered by its ID.",
            description = "Retrieves a list with a set of SubRuns filtered by Run ID or with a single SubRun filtered by its ID."
    )
    public List<SubRunDto> handleSubRunsQuery(@RequestParam(required = false) Long runId,
                                           @RequestParam(required = false) Long subRunId) {
        // TODO: implement service for SubRun ID
        if (runId != null) {
            return service.listAllByRunId(runId);
        } else {
            return null; // TODO: handle exception?
        }
    }

}
