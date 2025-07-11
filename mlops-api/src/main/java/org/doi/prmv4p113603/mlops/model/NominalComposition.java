package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "nominal_compositions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NominalComposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

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

    /*
     * NOTE: Lazy-loaded by default (because no fetch = FetchType.EAGER is set).
     *
     * Which means: after findAllById(...), the runs list will be empty unless:
     * - It is accessed later, triggering a lazy load (only works within a transactional context; i.e., in a @Transactional service method).
     * - It is explicitly fetched it in the query.
     */
    @OneToMany(mappedBy = "nominalComposition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Run> runs = new ArrayList<>();

}
