package org.doi.prmv4p113603.mlops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.exception.DuplicatedNominalCompositionException;
import org.doi.prmv4p113603.mlops.exception.NominalCompositionDeletionException;
import org.doi.prmv4p113603.mlops.exception.NominalCompositionNotFoundException;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer to encapsulate all business logic related to NominalComposition.
 */
@Service
@AllArgsConstructor
public class NominalCompositionService {

    private final NominalCompositionRepository nominalCompositionRepository;
    private final RunRepository runRepository;

    /**
     * Creates a Nominal Composition entry.
     * Returns HTTP 409 if the name already exists.
     */
    public NominalCompositionDto create(NominalCompositionDto dto) {

        if (nominalCompositionRepository.findByName(dto.getName()).isPresent()) {
            throw new DuplicatedNominalCompositionException(dto.getName());
        }

        NominalComposition saved = nominalCompositionRepository.save(dto.toEntity());

        // TODO: exception?
        return NominalCompositionDto.fromEntity(saved);

    }

    /**
     * Retrieves a NominalComposition by name.
     * Returns 404 if not found.
     */
    public NominalCompositionDto getByName(String name) {
        return nominalCompositionRepository.findByName(name)
                .map(NominalCompositionDto::fromEntity)
                .orElseThrow(() -> new NominalCompositionNotFoundException(name));
    }

    /**
     * Lists all NominalCompositions ordered by name.
     */
    public List<NominalCompositionDto> listAll() {
        return nominalCompositionRepository.findAllByOrderByNameAsc().stream()
                .map(NominalCompositionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update a Nominal Composition entry identified by its name.
     * Returns 404 if a Nominal Composition with the name passed does not exist.
     */
    public NominalCompositionDto updateByName(String name, NominalCompositionDto dto) {

        NominalComposition nc = nominalCompositionRepository.findByName(name)
                .orElseThrow(() -> new NominalCompositionNotFoundException(name));

        if (dto.getDescription() != null) {
            nc.setDescription(dto.getDescription());
        }

        nominalCompositionRepository.save(nc);

        return NominalCompositionDto.fromEntity(nc);

    }

    /**
     * Deletes a NominalComposition by name.
     * Returns 204 if deleted, 404 if not found.
     */
    public void deleteByName(String name) {

        NominalComposition nc = nominalCompositionRepository.findByName(name)
                .orElseThrow(() -> new NominalCompositionNotFoundException(name));

        if (runRepository.existsByNominalCompositionId(nc.getId())) {
            throw new NominalCompositionDeletionException(nc.getName());
        }

        nominalCompositionRepository.delete(nc);

    }

}
