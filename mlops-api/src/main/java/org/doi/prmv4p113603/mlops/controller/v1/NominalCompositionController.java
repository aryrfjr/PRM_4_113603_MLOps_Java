package org.doi.prmv4p113603.mlops.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.doi.prmv4p113603.mlops.data.dto.*;
import org.doi.prmv4p113603.mlops.service.NominalCompositionService;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing NominalComposition resources. It supports basic
 * CRUD operations and returns DTOs to ensure decoupling from internal models.
 */
@Tag(name = "CRUD")
@RestController
@RequestMapping("/api/v1/crud/nominal_compositions")
@Validated
public class NominalCompositionController {

    /*
     * NOTE: The Controller delegates business logic to the service, keeping it thin and focused.
     *  It is Model-Agnostic and only interacts with DTOs and the service layer.
     *
     * NOTE: Following the MVC Separation of Concerns the Controller only handles HTTP protocol,
     *  request validation, and response formatting; whereas the Service implements domain/business
     *  logic, the Repository manages persistence, and the Model stays in the data layer.
     *
     * TODO: Implement tests independently from persistence, mocking DTOs to assert against in
     *  unit/integration tests.
     */
    private final NominalCompositionService service;

    public NominalCompositionController(NominalCompositionService service) {
        this.service = service;
    }

    /**
     * Creates a Nominal Composition entry.
     */
    @PostMapping
    @Operation(
            summary = "Creates a Nominal Composition entry.",
            description = "Creates a Nominal Composition entry."
    )
    public ResponseEntity<NominalCompositionDto> create(@Valid @RequestBody NominalCompositionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Retrieves a NominalComposition by name.
     */
    @GetMapping("/{name}")
    @Operation(
            summary = "Retrieves a NominalComposition by name.",
            description = "Retrieves a NominalComposition by name."
    )
    public ResponseEntity<NominalCompositionDto> getByName(@PathVariable String name) {
        return ResponseEntity.ok(service.getByName(name));
    }

    /**
     * Lists all NominalCompositions ordered by name.
     */
    @GetMapping
    @Operation(
            summary = "Lists all NominalCompositions ordered by name.",
            description = "Lists all NominalCompositions ordered by name."
    )
    public List<NominalCompositionDto> listAll() {
        return service.listAll();
    }

    /**
     * Update a Nominal Composition entry identified by its name.
     */
    @PutMapping("/{name}")
    @Operation(
            summary = "Update a Nominal Composition entry identified by its name.",
            description = "Update a Nominal Composition entry identified by its name."
    )
    public ResponseEntity<NominalCompositionDto> updateByName(
            @PathVariable String name,
            @Valid @RequestBody NominalCompositionDto updateDto
    ) {
        return ResponseEntity.ok(service.updateByName(name, updateDto));
    }

    /**
     * Deletes a NominalComposition by name.
     */
    @DeleteMapping("/{name}")
    @Operation(
            summary = "Deletes a NominalComposition by name.",
            description = "Deletes a NominalComposition by name."
    )
    public ResponseEntity<?> deleteByName(@PathVariable String name) {
        service.deleteByName(name);
        return ResponseEntity.noContent().build();
    }

}
