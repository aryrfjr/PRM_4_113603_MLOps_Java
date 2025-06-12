package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubRun> subRuns;
}
