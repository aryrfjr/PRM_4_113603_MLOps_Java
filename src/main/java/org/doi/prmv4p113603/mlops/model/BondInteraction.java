package org.doi.prmv4p113603.mlops.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bonds_interactions", indexes = {
        @Index(name = "idx_bonds_sub_run", columnList = "sub_run_id"),
        @Index(name = "idx_bonds_atom_pair", columnList = "atom_a_element, atom_b_element"),
        @Index(name = "idx_bonds_distance", columnList = "bond_distance")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sub_run_id", "atom_a_idx", "atom_b_idx"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BondInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_run_id", nullable = false)
    private SubRun subRun;

    @Column(name = "atom_a_idx", nullable = false)
    private int atomAIdx;

    @Column(name = "atom_b_idx", nullable = false)
    private int atomBIdx;

    @Column(name = "atom_a_element", nullable = false, length = 5)
    private String atomAElement;

    @Column(name = "atom_b_element", nullable = false, length = 5)
    private String atomBElement;

    @Column(name = "bond_distance", nullable = false)
    private double bondDistance;

    @Column(name = "bond_icohp")
    private Double bondICOHP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descriptor_file_id")
    private DescriptorFile descriptorFile;
}
