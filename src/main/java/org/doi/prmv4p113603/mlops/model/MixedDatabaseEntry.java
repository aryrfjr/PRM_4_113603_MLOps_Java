package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mixed_database_entries", indexes = {
        @Index(name = "idx_mixed_entries_db_id", columnList = "mixed_db_id"),
        @Index(name = "idx_mixed_entries_bond_type", columnList = "bond_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class MixedDatabaseEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mixed_db_id", nullable = false)
    private MixedDatabase mixedDb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descriptor_file_id", nullable = false)
    private DescriptorFile descriptorFile;

    @Column(name = "bond_type", nullable = false, length = 20)
    private String bondType;
}
