package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.doi.prmv4p113603.mlops.data.dto.*;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;

import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing NominalComposition resources.
 * <p>
 * Supports basic CRUD operations and returns DTOs to ensure decoupling from internal models.
 */
@Tag(name = "CRUD")
@RestController
@RequestMapping("/api/v1/nominal-compositions")
@Validated
public class NominalCompositionController {

    private final NominalCompositionRepository repository;

    public NominalCompositionController(NominalCompositionRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a Nominal Composition entry.
     * Returns HTTP 409 if the name already exists.
     */
    @PostMapping
    @Operation(
            summary = "Creates a Nominal Composition entry.",
            description = "Creates a Nominal Composition entry."
    )
    public ResponseEntity<NominalCompositionResponseDto> create(@Valid @RequestBody NominalCompositionCreateDto dto) {
        if (repository.findByName(dto.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        NominalComposition saved = repository.save(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NominalCompositionResponseDto.fromEntity(saved));
    }

    /**
     * Retrieves a NominalComposition by name.
     * Returns 404 if not found.
     */
    @GetMapping("/{name}")
    @Operation(
            summary = "Retrieves a NominalComposition by name.",
            description = "Retrieves a NominalComposition by name."
    )
    public ResponseEntity<NominalCompositionResponseDto> getByName(@PathVariable String name) {
        return repository.findByName(name)
                .map(NominalCompositionResponseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lists all NominalCompositions ordered by name.
     */
    @GetMapping
    @Operation(
            summary = "Lists all NominalCompositions ordered by name.",
            description = "Lists all NominalCompositions ordered by name."
    )
    public List<NominalCompositionResponseDto> listAll() {
        return repository.findAll().stream()
                .map(NominalCompositionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update a Nominal Composition entry identified by its name.
     * Returns 404 if the resource does not exist.
     */
    @PutMapping("/{name}")
    @Operation(
            summary = "Update a Nominal Composition entry identified by its name.",
            description = "Update a Nominal Composition entry identified by its name."
    )
    public ResponseEntity<NominalCompositionResponseDto> updateByName(
            @PathVariable String name,
            @Valid @RequestBody NominalCompositionCreateDto updateDto
    ) {
        Optional<NominalComposition> optional = repository.findByName(name);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        NominalComposition nc = optional.get();
        nc.setName(updateDto.getName());
        if (updateDto.getDescription() != null) {
            nc.setDescription(updateDto.getDescription());
        }
        repository.save(nc);

        return ResponseEntity.ok(NominalCompositionResponseDto.fromEntity(nc));
    }

    /**
     * Deletes a NominalComposition by name.
     * Returns 204 if deleted, 404 if not found.
     */
    @DeleteMapping("/{name}")
    @Operation(
            summary = "Deletes a NominalComposition by name.",
            description = "Deletes a NominalComposition by name."
    )
    public ResponseEntity<?> deleteByName(@PathVariable String name) {
        return repository.findByName(name).map(nc -> {
            repository.delete(nc);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
