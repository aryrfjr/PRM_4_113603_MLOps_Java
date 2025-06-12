package org.doi.prmv4p113603.mlops.controller.v1;

import org.doi.prmv4p113603.mlops.data.dto.*;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing NominalComposition resources.
 * <p>
 * Supports basic CRUD operations and returns DTOs to ensure decoupling from internal models.
 */
@RestController
@RequestMapping("/api/nominal_compositions")
public class NominalCompositionController {

    private final NominalCompositionRepository repository;

    public NominalCompositionController(NominalCompositionRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new NominalComposition.
     * Returns HTTP 409 if the name already exists.
     */
    @PostMapping
    public ResponseEntity<NominalCompositionResponseDto> create(@RequestBody NominalCompositionCreateDto dto) {
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
    public List<NominalCompositionResponseDto> listAll() {
        return repository.findAll().stream()
                .map(NominalCompositionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a NominalComposition by name.
     * Returns 204 if deleted, 404 if not found.
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteByName(@PathVariable String name) {
        return repository.findByName(name).map(nc -> {
            repository.delete(nc);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
