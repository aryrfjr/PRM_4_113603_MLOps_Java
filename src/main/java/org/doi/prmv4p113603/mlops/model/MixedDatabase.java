package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "mixed_databases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MixedDatabase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "mixedDb", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MixedDatabaseEntry> entries;
}
