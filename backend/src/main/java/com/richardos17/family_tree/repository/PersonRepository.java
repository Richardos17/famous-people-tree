package com.richardos17.family_tree.repository;

import com.richardos17.family_tree.domain.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, String> {
    @Query("""
            MATCH (p:Person {localId: $id})

            OPTIONAL MATCH (p)-[rP:HAS_PARENT]->(parent)
            OPTIONAL MATCH (country)-[rB:BORN_IN]-(p)
            OPTIONAL MATCH (spouse)-[rM:MARRIED_TO]-(p)

            RETURN p,
                   collect(DISTINCT rP),
                   collect(DISTINCT parent),
                   collect(DISTINCT rM),
                   collect(DISTINCT spouse),
                   collect(DISTINCT country)
    """)
    Optional<Person> findByLocalId(String id);
    @Query("""
            MATCH (p:Person {wikidataId: $id})

            OPTIONAL MATCH (country)-[rB:BORN_IN]-(p)

            RETURN p,
                               collect(DISTINCT rB),

                                      collect(DISTINCT country)
    """)
    Optional<Person> findByWikidataId(String id);

    @Query("""
            MATCH (p:Person {wikidataId: $id})

            OPTIONAL MATCH (p)-[rP:HAS_PARENT]->(parent)
            OPTIONAL MATCH (country)-[rB:BORN_IN]-(p)
            OPTIONAL MATCH (spouse)-[rM:MARRIED_TO]-(p)

            RETURN p,
                   collect(DISTINCT rP),
                   collect(DISTINCT parent),
                   collect(DISTINCT rM),
                   collect(DISTINCT spouse),
                   collect(DISTINCT country)
    """)
    Optional<Person> findFullByWikidataId(String id);

    @Query("""
            MATCH (p:Person)
            WHERE toLower(p.name) CONTAINS toLower($name)

            OPTIONAL MATCH (p)-[:BORN_IN]->(country)

            RETURN p,
                   collect(DISTINCT country)
    """)
    List<Person> searchByName(String name);
}
