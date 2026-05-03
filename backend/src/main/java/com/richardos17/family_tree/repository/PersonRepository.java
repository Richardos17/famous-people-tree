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
    
    // parents (depth 1)
    OPTIONAL MATCH (p)-[:HAS_PARENT]->(parent)
    
    
    // children (depth 1)
    OPTIONAL MATCH (child)-[:HAS_PARENT]->(p)
    
    RETURN p,
           collect(DISTINCT parent) AS parents,
           collect(DISTINCT child) AS children
    """)
    Optional<Person> findByLocalId(String id);

    List<Person> findByName(String name);
    
    List<Person> findByNameContainingIgnoreCase(String name);

    @Query("""
    MATCH (p:Person)<-[r:HAS_PARENT]-(:Person {localId: $id})
    RETURN
        p AS entity,
        r.type AS type
                          """)
    List<FamilyRelationship<Person>> findParentsByChildId(String id);
    
    @Query("""
    MATCH (child:Person)-[r:HAS_PARENT]->(:Person {localId: $id})
    RETURN 
        child AS entity,
        r.type AS type
                          """)
    List<FamilyRelationship<Person>> findChildrenByParentId(String id);
}
