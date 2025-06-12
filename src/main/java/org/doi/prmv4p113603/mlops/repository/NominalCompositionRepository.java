package org.doi.prmv4p113603.mlops.repository;

import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * DAO for NominalComposition entity. It provides built-in CRUD operations
 * and custom find methods.
 */
public interface NominalCompositionRepository extends JpaRepository<NominalComposition, Long> {

    /*
     * NOTE: The methods findByName and findAllByOrderByNameAsc are automatically
     *  implemented by Spring Data JPA at runtime based on the method names.
     *  Spring Data JPA uses a feature called query method name parsing, where it
     *  interprets the names of methods defined in a JpaRepository (or similar
     *  Spring Data interface) and generates the corresponding SQL/HQL queries.
     *  See more about the limits of Spring Data’s query derivation syntax
     *  at: https://docs.spring.io/spring-data/jpa/reference/#jpa.query-methods.query-creation
     */

    Optional<NominalComposition> findByName(String name);

    List<NominalComposition> findAllByOrderByNameAsc();

}
