package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;
import org.doi.prmv4p113603.mlops.domain.SimulationStatus;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

/**
 * This entity representing a Run; a set of simulations (they make
 * this MLOps system physics-informed) which will explore the configuration
 * space for a given Nominal Composition (NC). It can be used to: (i) improve data
 * coverage (configurational diversity in terms of structure) without creating
 * new NCs; (ii) or bring better generalization by creating 100-atom cells for new NCs.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "runs", indexes = {
        @Index(name = "idx_runs_nominal_composition", columnList = "nominal_composition_id"),
        @Index(name = "idx_runs_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nominal_composition_id", "run_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nominal_composition_id", nullable = false)
    private NominalComposition nominalComposition;

    @Column(name = "run_number", nullable = false)
    private int runNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SimulationStatus status = SimulationStatus.SCHEDULED;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubRun> subRuns;

}
