package org.doi.prmv4p113603.mlops.data.dto;

import org.doi.prmv4p113603.mlops.model.NominalComposition;
import lombok.*;

import java.time.Instant;

/**
 * DTO used to send NominalComposition data back to the client. It
 * includes ID and timestamps, reflecting data persisted in the database.
 *
 * @see org.doi.prmv4p113603.mlops.model.NominalComposition
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NominalCompositionResponseDto {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;

    public static NominalCompositionResponseDto fromEntity(NominalComposition entity) {
        return NominalCompositionResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
