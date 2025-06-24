package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer to encapsulate all business logic related to NominalComposition.
 */
@Service
public class NominalCompositionService {

    private final NominalCompositionRepository repository;

    public NominalCompositionService(NominalCompositionRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a Nominal Composition entry.
     * Returns HTTP 409 if the name already exists.
     */
    public NominalCompositionDto create(NominalCompositionDto dto) {

        if (repository.findByName(dto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nominal composition already exists");
        }

        NominalComposition saved = repository.save(dto.toEntity());

        // TODO: exception?
        return NominalCompositionDto.fromEntity(saved);

    }

    /**
     * Retrieves a NominalComposition by name.
     * Returns 404 if not found.
     */
    public NominalCompositionDto getByName(String name) {
        return repository.findByName(name)
                .map(NominalCompositionDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
    }

    /**
     * Lists all NominalCompositions ordered by name.
     */
    public List<NominalCompositionDto> listAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(NominalCompositionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update a Nominal Composition entry identified by its name.
     * Returns 404 if a Nominal Composition with the name passed does not exist.
     */
    public NominalCompositionDto updateByName(String name, NominalCompositionDto dto) {

        NominalComposition nc = repository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Nominal Composition '" + name + "' doesn't exist."));

        if (dto.getDescription() != null) {
            nc.setDescription(dto.getDescription());
        }

        repository.save(nc);

        return NominalCompositionDto.fromEntity(nc);

    }

    /**
     * Deletes a NominalComposition by name.
     * Returns 204 if deleted, 404 if not found.
     */
    public void deleteByName(String name) {

        NominalComposition nc = repository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        // TODO: exception?
        repository.delete(nc);

    }

}
