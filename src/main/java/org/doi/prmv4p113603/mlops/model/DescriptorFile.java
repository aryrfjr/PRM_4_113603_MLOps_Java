package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "descriptor_files", indexes = {
        @Index(name = "idx_descriptor_sub_run", columnList = "sub_run_id"),
        @Index(name = "idx_descriptor_bond_type", columnList = "bond_type")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sub_run_id", "bond_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescriptorFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_run_id", nullable = false)
    private SubRun subRun;

    @Column(name = "bond_type", nullable = false, length = 20)
    private String bondType;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "descriptorFile")
    private List<BondInteraction> bonds;
}
