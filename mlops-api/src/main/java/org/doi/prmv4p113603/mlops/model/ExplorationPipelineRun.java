package org.doi.prmv4p113603.mlops.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.doi.prmv4p113603.mlops.data.response.ExplorationPipelineRunResponse;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "exploration_pipeline_runs")
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

    // Optional: could be a DTO/POJO instead of Map if preferred
    // private ExplorationResponse responsePayload;

    @OneToMany
    @JoinTable(
            name = "exploration_pipeline_runs_runs",
            joinColumns = @JoinColumn(name = "exploration_pipeline_run_id"),
            inverseJoinColumns = @JoinColumn(name = "run_id")
    )
    private List<Run> runs;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
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

}
