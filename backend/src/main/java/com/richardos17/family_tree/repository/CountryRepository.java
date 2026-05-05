package com.richardos17.family_tree.repository;

import com.richardos17.family_tree.domain.Country;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends Neo4jRepository<Country, String> {
    Optional<Country> findByName(String name);
}