package org.doi.prmv4p113603.simops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.common.service.MinioStorageService;
import org.doi.prmv4p113603.simops.data.dto.SimulationJobDto;
import org.doi.prmv4p113603.simops.data.request.SimulationJobRequest;
import org.doi.prmv4p113603.simops.domain.SimulationJobStatus;
import org.doi.prmv4p113603.simops.model.SimulationJob;
import org.doi.prmv4p113603.simops.repository.SimulationJobRepository;
import org.doi.prmv4p113603.common.util.MinioUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mocking an HPC service.
 */
@Service
@AllArgsConstructor
public class SimOpsService {

    private final SimulationJobRepository repository;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final MinioStorageService minioStorageService;

    public SimulationJobDto submitJob(SimulationJobRequest request) {

        SimulationJob job = SimulationJob.builder()
                .inputFile(request.getInputFile())
                .outputFiles(request.getOutputFiles())
                .status(SimulationJobStatus.QUEUED)
                .submittedAt(Instant.now())
                .build();

        // TODO: check if input file exists in MinIO

        // Handle dependency
        if (request.getDependsOnJobId() != null) {

            SimulationJob parent = repository.findById(request.getDependsOnJobId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid dependency ID"));

            job.setDependsOn(parent);

        }

        SimulationJob savedJob = repository.save(job);

        /*
         * NOTE: taskExecutor is the bean created by the method taskExecutor() from
         *  org.doi.prmv4p113603.simops.config.SimOpsConfig.
         *
         * NOTE: execute() is a method from java.util.concurrent.Executor.
         *
         * NOTE: () -> simulateJobExecution(request) is a lambda expression that creates a
         *  Runnable task. It will submit a task (lambda) to the taskExecutor (which is a
         *  ThreadPoolTaskExecutor).The created Runnable task is a call to the private method
         *  simulateJobExecution, and it is submitted asynchronously (i.e., it doesn't block
         *  the caller. The thread pool decides when to run it based on availability.
         */
        taskExecutor.execute(() -> simulateJobExecution(savedJob.getId()));

        return SimulationJobDto.fromEntity(savedJob);

    }

    public SimulationJobDto getJob(Long id) {
        return SimulationJobDto.fromEntity(repository.findById(id).orElseThrow());
    }

    public Page<SimulationJobDto> getJobsByStatus(SimulationJobStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(SimulationJobDto::fromEntity);
    }

    public Page<SimulationJobDto> getAllJobs(Pageable pageable) {
        return repository.findAll(pageable).map(SimulationJobDto::fromEntity);
    }

    public int getRunningJobCount() {
        return taskExecutor.getActiveCount();
    }

    public int getQueuedJobCount() {
        return taskExecutor.getThreadPoolExecutor().getQueue().size();
    }

    public Map<String, Integer> getJobExecutionStatus() {
        Map<String, Integer> stats = new HashMap<>();

        stats.put("runningJobs", getRunningJobCount());
        stats.put("queuedJobs", getQueuedJobCount());
        stats.put("dbStatusCount", repository.countByStatus(SimulationJobStatus.RUNNING)); // TODO: review usage

        return stats;
    }

    // TODO: Add Job Cancellation Logic

    /*
     * Helpers
     */

    private void simulateJobExecution(Long jobId) {

        SimulationJob job = repository.findByIdWithOutputFiles(jobId).orElseThrow();

        // Wait for dependency to complete, if any
        if (job.getDependsOn() != null) {

            Long parentId = job.getDependsOn().getId();

            // TODO: Can be configurable (e.g., wait even on FAILED, allow retry, etc.).
            while (true) {

                // TODO: Not working exactly as expected. The issue is that if two chains of
                //  dependent jobs are submitted, the next head job only starts running after
                //  the tail job of the previous run started.

                SimulationJob parent = repository.findById(parentId).orElse(null);

                if (parent == null || parent.getStatus() == SimulationJobStatus.COMPLETED) {
                    break; // Parent job finished successfully; so, proceed with this job
                } else if (parent.getStatus() == SimulationJobStatus.FAILED ||
                        parent.getStatus() == SimulationJobStatus.CANCELLED) {

                    job.setStatus(SimulationJobStatus.FAILED);
                    job.setCompletedAt(Instant.now());

                    repository.save(job);

                    return; // Cancel this job: dependency failed or was cancelled

                }

                try {

                    /*
                     * NOTE: Polling with a sleep interval here is on purpose;
                     *  it is a common, lightweight pattern for the job dependency check,
                     *  resource availability waits, and mocking async queue systems (like Slurm).
                     */

                    // Poll every 10 seconds before check again if the job can run
                    Thread.sleep(10000);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    job.setStatus(SimulationJobStatus.CANCELLED);
                    job.setCompletedAt(Instant.now());

                    repository.save(job);

                    return; // Job was cancelled mid-wait

                }

            }

        }

        // Proceed with job execution
        try {

            job.setStatus(SimulationJobStatus.RUNNING);
            job.setStartedAt(Instant.now());

            repository.save(job);

            Thread.sleep(10000); // Simulate run time (10 seconds) // TODO: go to docker-compose.dev.yml

            job.setStatus(SimulationJobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());

            // Writing the (pre-existing) output files to MinIO
            uploadSimulationOutputFilesToS3(job.getOutputFiles());

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            job.setStatus(SimulationJobStatus.FAILED);

        } finally {
            repository.save(job);
        }

    }

    private void uploadSimulationOutputFilesToS3(List<String> outputFilePaths) {
        outputFilePaths.forEach(path -> minioStorageService.uploadFile(MinioUtils.pathToKey(path), path));
    }

}
