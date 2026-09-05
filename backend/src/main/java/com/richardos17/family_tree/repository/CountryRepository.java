package com.richardos17.family_tree.repository;

import com.richardos17.family_tree.domain.Country;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends Neo4jRepository<Country, String> {
    Optional<Country> findByName(String name);
    Boolean existsByLocalId(String localId);
    Boolean existsByWikidataId(String s);
    Country getCountryByWikidataId(String wikidataId);
    Optional<Country> findByWikidataId(String wikidataId);

    @Query("MERGE (c:Country {wikidataId: $wikidataId}) ON CREATE SET c.name = $name, c.localId = randomUUID() RETURN c")
    Country mergeByWikidataId(String wikidataId, String name);
}
