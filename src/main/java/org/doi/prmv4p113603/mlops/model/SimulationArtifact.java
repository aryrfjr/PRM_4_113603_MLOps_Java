package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "simulation_artifacts", indexes = {
        @Index(name = "idx_artifact_sub_run", columnList = "sub_run_id"),
        @Index(name = "idx_artifact_type", columnList = "artifact_type")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sub_run_id", "artifact_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SimulationArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_run_id", nullable = false)
    private SubRun subRun;

    @Column(name = "artifact_type", nullable = false, length = 50)
    private String artifactType;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
