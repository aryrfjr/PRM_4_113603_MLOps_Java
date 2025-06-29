package org.doi.prmv4p113603.simops.repository;

import org.doi.prmv4p113603.simops.domain.SimulationJobStatus;
import org.doi.prmv4p113603.simops.model.SimulationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SimulationJobRepository extends JpaRepository<SimulationJob, Long> {

    Page<SimulationJob> findByStatus(SimulationJobStatus status, Pageable pageable);

    Page<SimulationJob> findAll(Pageable pageable);

    Integer countByStatus(SimulationJobStatus status);

}
