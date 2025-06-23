package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    /*
     * NOTE: Lazy-loaded by default (because no fetch = FetchType.EAGER is set).
     *
     * Which means: after findAllById(...), the runs list will be empty unless:
     * - It is accessed later, triggering a lazy load (only works within a transactional context; i.e., in a @Transactional service method).
     * - It is explicitly fetched it in the query.
     */
    @OneToMany(mappedBy = "nominalComposition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Run> runs;

}
