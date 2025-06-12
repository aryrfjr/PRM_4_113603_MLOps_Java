package org.doi.prmv4p113603.mlops.data.dto;

import jakarta.validation.constraints.NotBlank;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import lombok.*;

import java.time.Instant;

/**
 * DTO used to receive client input for creating a NominalComposition entity. It
 * contains only the fields that can be safely and meaningfully set by the user.
 *
 * @see org.doi.prmv4p113603.mlops.model.NominalComposition
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NominalCompositionCreateDto {

    /*
     * NOTE: This approach is a refinement (not a new version) of the classic DTO pattern in which different
     *  classes (Multiple DTOs like Create, Response, etc.) are defined for each direction or phase of
     *  communication. This pattern is useful when validation needs differ, for avoiding issues like
     *  leaking internal database fields, and to ensure cleaner contracts (each DTO serves a clear,
     *  single responsibility in the request/response cycle).
     */

    @NonNull
    @NotBlank(message = "Name must not be blank")
    private String name;

    private String description;

    public NominalComposition toEntity() {
        return NominalComposition.builder()
                .name(this.name)
                .description(this.description)
                .createdAt(Instant.now())
                .build();
    }
}
