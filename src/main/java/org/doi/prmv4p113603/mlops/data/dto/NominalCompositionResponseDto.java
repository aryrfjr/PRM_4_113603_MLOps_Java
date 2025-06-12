package org.doi.prmv4p113603.mlops.data.dto;

import org.doi.prmv4p113603.mlops.model.NominalComposition;
import lombok.*;

import java.time.Instant;

/**
 * DTO used to send NominalComposition data back to the client.
 * <p>
 * Includes ID and timestamps, reflecting data persisted in the database.
 * <p>
 * NOTE: This approach is a refinement (not a new version) of the classic DTO pattern in which different
 * classes (Multiple DTOs like Create, Response, etc.) are defined for each direction or phase of
 * communication. This pattern is useful when validation needs differ, for avoiding issues like
 * leaking internal database fields, and to ensure cleaner contracts (each DTO serves a clear,
 * single responsibility in the request/response cycle).
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
