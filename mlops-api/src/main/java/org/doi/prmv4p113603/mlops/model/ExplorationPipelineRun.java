package org.doi.prmv4p113603.mlops.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.doi.prmv4p113603.mlops.data.response.ExplorationPipelineRunResponse;
import org.doi.prmv4p113603.mlops.domain.PipelineRunStatus;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;

/**
 * This entity represents an exploration pipeline run; a request to an
 * orchestration engine (in this case Airflow).
 */
@Entity
@Table(name = "exploration_pipeline_runs", indexes = {
        @Index(name = "idx_exploration_pipeline_runs_runs", columnList = "run_id"),
        @Index(name = "idx_exploration_pipeline_runs_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"run_id", "external_pipeline_run_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplorationPipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JSONB payload
    @Type(value = JsonBinaryType.class)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private ExplorationPipelineRunResponse responsePayload;

    @Column(name = "external_pipeline_id", nullable = false, updatable = false)
    private String externalPipelineId;

    @Column(name = "external_pipeline_run_id", nullable = false, updatable = false)
    private String externalPipelineRunId;

    @OneToMany
    @JoinTable(
            name = "exploration_pipeline_runs_runs",
            joinColumns = @JoinColumn(name = "exploration_pipeline_run_id"),
            inverseJoinColumns = @JoinColumn(name = "run_id")
    )
    private List<Run> runs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PipelineRunStatus status = PipelineRunStatus.SCHEDULED;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
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

}
