package org.doi.prmv4p113603.simops.controller.v1;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.simops.domain.SimulationJobStatus;
import org.doi.prmv4p113603.simops.model.SimulationJob;
import org.doi.prmv4p113603.simops.service.SimOpsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for mocking an HPC service.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/jobs")
public class SimOpsController {

    private final SimOpsService jobService;

    @PostMapping
    public SimulationJob submitJob(@RequestParam String inputPath,
                                   @RequestParam String outputPath) {
        return jobService.submitJob(inputPath, outputPath);
    }

    @GetMapping("/{id}")
    public SimulationJob getJob(@PathVariable Long id) {
        return jobService.getJob(id);
    }

    @GetMapping("/squeue")
    public Page<SimulationJob> listJobsByStatus(
            @RequestParam SimulationJobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobService.getJobsByStatus(status, pageable);
    }

    @GetMapping
    public Page<SimulationJob> listAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobService.getAllJobs(pageable);
    }

}
