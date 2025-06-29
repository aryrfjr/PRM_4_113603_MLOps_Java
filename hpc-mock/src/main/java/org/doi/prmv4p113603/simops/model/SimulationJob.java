package org.doi.prmv4p113603.simops.model;

import jakarta.persistence.*;
import lombok.*;
import org.doi.prmv4p113603.simops.domain.SimulationJobStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String inputFile;
    private SimulationJobStatus status;

    @ElementCollection(fetch = FetchType.EAGER) // load all output files, they won't be more than 3 files
    @CollectionTable(name = "simulation_job_outputs", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "output_file")
    private List<String> outputFiles = new ArrayList<>();

    private Instant submittedAt;
    private Instant startedAt;
    private Instant completedAt;

    /*
     * NOTE: Creates a self-referencing foreign key (depends_on_job_id) in the simulation_job table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_job_id")
    private SimulationJob dependsOn;

}
