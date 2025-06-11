package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "sub_runs", indexes = {
        @Index(name = "idx_sub_runs_run_id", columnList = "run_id"),
        @Index(name = "idx_sub_runs_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"run_id", "sub_run_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SubRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private Run run;

    @Column(name = "sub_run_number", nullable = false)
    private int subRunNumber;

    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED";

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "subRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DescriptorFile> descriptorFiles;

    @OneToMany(mappedBy = "subRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SimulationArtifact> simulationArtifacts;

    @OneToMany(mappedBy = "subRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BondInteraction> bonds;
}
