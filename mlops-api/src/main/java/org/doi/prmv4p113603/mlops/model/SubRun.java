package org.doi.prmv4p113603.mlops.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

/**
 * This entity representing a SubRun; a sub-set of simulations (they also make
 * this MLOps system physics-informed) which will exploit the configuration
 * space for a given Run created for a Nominal Composition. It is related
 * to data augmentation (shear, tension, compression) on existing 100-atom cells
 * when the structural diversity of current configs needs improvement without
 * having to create new Runs.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
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
public class SubRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private Run run;

    @Column(name = "sub_run_number", nullable = false)
    private int subRunNumber;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "external_pipeline_run_id")
    private String externalPipelineRunId;

    @OneToMany(mappedBy = "subRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DescriptorFile> descriptorFiles;

    @OneToMany(mappedBy = "subRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SimulationArtifact> simulationArtifacts;

    @OneToMany(mappedBy = "subRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BondInteraction> bonds;

}
