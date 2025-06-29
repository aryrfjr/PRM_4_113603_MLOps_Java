package org.doi.prmv4p113603.simops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.simops.domain.SimulationJobStatus;
import org.doi.prmv4p113603.simops.model.SimulationJob;
import org.doi.prmv4p113603.simops.repository.SimulationJobRepository;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Mocking an HPC service.
 */
@Service
@AllArgsConstructor
public class SimOpsService {

    private final SimulationJobRepository repository;
    private final TaskExecutor taskExecutor;

    public SimulationJob submitJob(String inputFile, String outputFile) {

        SimulationJob job = SimulationJob.builder()
                .inputFile(inputFile)
                .outputFile(outputFile)
                .status("QUEUED")
                .submittedAt(Instant.now())
                .build();

        SimulationJob savedJob = repository.save(job);

        taskExecutor.execute(() -> simulateJobExecution(savedJob.getId()));

        return savedJob;

    }

    private void simulateJobExecution(Long jobId) {

        SimulationJob job = repository.findById(jobId).orElseThrow();

        try {

            job.setStatus("RUNNING");
            job.setStartedAt(Instant.now());
            repository.save(job);

            // Simulate queue delay
            Thread.sleep(3000); // queued delay
            // Simulate execution
            Thread.sleep(5000); // run time delay

            job.setStatus("COMPLETED");
            job.setCompletedAt(Instant.now());

            // TODO: simulate writing a file to MinIO

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            job.setStatus("FAILED");

        } finally {
            repository.save(job);
        }

    }

    public SimulationJob getJob(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public Page<SimulationJob> getJobsByStatus(SimulationJobStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    public Page<SimulationJob> getAllJobs(Pageable pageable) {
        return repository.findAll(pageable);
    }

}
