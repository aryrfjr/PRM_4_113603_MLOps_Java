package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionCreateDto;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionResponseDto;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    public NominalCompositionResponseDto create(NominalCompositionCreateDto dto) {
        if (repository.findByName(dto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nominal composition already exists");
        }
        NominalComposition saved = repository.save(dto.toEntity());
        return NominalCompositionResponseDto.fromEntity(saved);
    }

    /**
     * Retrieves a NominalComposition by name.
     * Returns 404 if not found.
     */
    public NominalCompositionResponseDto getByName(String name) {
        return repository.findByName(name)
                .map(NominalCompositionResponseDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
    }

    /**
     * Lists all NominalCompositions (TODO: ordered by name).
     */
    public List<NominalCompositionResponseDto> listAll() {
        return repository.findAll().stream()
                .map(NominalCompositionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update a Nominal Composition entry identified by its name.
     * Returns 404 if the resource does not exist.
     */
    public NominalCompositionResponseDto updateByName(String name, NominalCompositionCreateDto dto) {
        NominalComposition nc = repository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        nc.setName(dto.getName());
        if (dto.getDescription() != null) {
            nc.setDescription(dto.getDescription());
        }
        repository.save(nc);
        return NominalCompositionResponseDto.fromEntity(nc);
    }

    /**
     * Deletes a NominalComposition by name.
     * Returns 204 if deleted, 404 if not found.
     */
    public void deleteByName(String name) {
        NominalComposition nc = repository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
        repository.delete(nc);
    }

}
