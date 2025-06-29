package org.doi.prmv4p113603.simops.controller.v1;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.simops.data.dto.SimulationJobDto;
import org.doi.prmv4p113603.simops.data.request.SimulationJobRequest;
import org.doi.prmv4p113603.simops.domain.SimulationJobStatus;
import org.doi.prmv4p113603.simops.model.SimulationJob;
import org.doi.prmv4p113603.simops.service.SimOpsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for mocking an HPC service.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/jobs")
public class SimOpsController {

    private final SimOpsService jobService;

    @PostMapping
    public SimulationJobDto submitJob(@Valid @RequestBody SimulationJobRequest request) {
        return jobService.submitJob(request);
    }

    @GetMapping("/{id}")
    public SimulationJobDto getJob(@PathVariable Long id) {
        return jobService.getJob(id);
    }

    @GetMapping("/squeue")
    public Page<SimulationJobDto> listJobsByStatus(
            @RequestParam SimulationJobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobService.getJobsByStatus(status, pageable);
    }

    @GetMapping
    public Page<SimulationJobDto> listAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobService.getAllJobs(pageable);
    }

    @GetMapping("/monitor")
    public ResponseEntity<Map<String, Integer>> getThreadPoolStats() {
        return ResponseEntity.ok(jobService.getJobExecutionStatus());
    }

}
