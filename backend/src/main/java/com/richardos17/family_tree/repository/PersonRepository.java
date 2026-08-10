package com.richardos17.family_tree.repository;

import com.richardos17.family_tree.domain.FamilyRelationshipDTO;
import com.richardos17.family_tree.domain.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
            MATCH (p:Person)
            WHERE toLower(p.name) CONTAINS toLower($name)

            OPTIONAL MATCH (p)-[:BORN_IN]->(country)

            RETURN p,
                   collect(DISTINCT country)
    """)
    List<Person> searchByName(String name);

    @Query("""
    MATCH (parent:Person {localId: $parentLocalId})
    MATCH (child:Person {localId: $childLocalId})
    MERGE (child)-[:HAS_PARENT]->(parent)
    """)
    void createParentRelationship(String parentLocalId, String childLocalId);

    @Query("""
    MATCH (person1:Person {localId: $spouse1LocalId})
    MATCH (person2:Person {localId: $spouse2LocalId})
    MERGE (person1)-[r:MARRIED_TO]->(person2)
    SET r.start_date = $startDate,
        r.end_date = $endDate
    """)
    void createMarriageRelationship(
            String spouse1LocalId,
            String spouse2LocalId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
    MATCH (parent:Person)-[r:HAS_PARENT]->(child:Person)
    WHERE child.localId = $childId
    RETURN parent.localId AS parentId,
           child.localId AS childId""")
    List<FamilyRelationshipDTO> findParents(String childId);

    @Query("""
    MATCH (parent:Person)-[r:HAS_PARENT]->(child:Person)
    WHERE parent.localId = $parentId
    RETURN parent.localId AS parentId,
           child.localId AS childId""")
    List<FamilyRelationshipDTO> findChildren(String parentId);


}
