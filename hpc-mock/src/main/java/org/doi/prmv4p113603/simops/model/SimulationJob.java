package org.doi.prmv4p113603.simops.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

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
    private String status; // TODO: QUEUED, RUNNING, COMPLETED, FAILED
    private String outputFile;

    private Instant submittedAt;
    private Instant startedAt;
    private Instant completedAt;

}
