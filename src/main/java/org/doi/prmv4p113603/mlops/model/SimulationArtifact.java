package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;
import org.doi.prmv4p113603.mlops.domain.SimulationArtifactType;

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
public class SimulationArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_run_id", nullable = false)
    private SubRun subRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type", nullable = false, length = 50)
    private SimulationArtifactType artifactType;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "checksum", length = 64)
    private String checksum; // TODO: Index this column for lookup; use it to detect duplicate artifacts

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
