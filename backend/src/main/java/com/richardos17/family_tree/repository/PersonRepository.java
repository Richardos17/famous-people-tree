package com.richardos17.family_tree.repository;

import com.richardos17.family_tree.domain.FamilyRelationship;
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
            MATCH (p:Person {name: $name})

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
    List<Person> findByName(String name);
    @Query("""
            MATCH (p:Person)
            WHERE toLower(p.name) CONTAINS toLower($name)

            OPTIONAL MATCH (p)-[rP:HAS_PARENT]->(parent)
            OPTIONAL MATCH (p)-[:BORN_IN]->(country)
            OPTIONAL MATCH (spouse)-[rM:MARRIED_TO]-(p)

            RETURN p,
                   collect(DISTINCT rP),
                   collect(DISTINCT parent),
                   collect(DISTINCT rM),
                   collect(DISTINCT spouse),
                   collect(DISTINCT country)
    """)
    List<Person> searchByName(String name);

    @Query("""
            MATCH (parent:Person)<-[r:HAS_PARENT]-(:Person {localId: $id})
            RETURN
                parent AS entity,
                r.type AS type
                          """)
    List<FamilyRelationship> findParentsByChildId(String id);
    
    @Query("""
            MATCH (child:Person)-[r:HAS_PARENT]->(:Person {localId: $id})
            RETURN
                child AS entity,
                r.type AS type
                          """)
    List<FamilyRelationship> findChildrenByParentId(String id);
}
