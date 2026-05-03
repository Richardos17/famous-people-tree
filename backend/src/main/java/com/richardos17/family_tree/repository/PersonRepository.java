package com.richardos17.family_tree.repository;

import com.richardos17.family_tree.domain.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, String> {
    List<Person> findByName(String name);
    
    List<Person> findByNameContainingIgnoreCase(String name);

    @Query("MATCH (p:Person)<-[:HAS_PARENT]-(:Person{id:$id}) RETURN p")
    List<Person> findParentsByChildId(String id);

    @Query("MATCH (p:Person)-[:HAS_PARENT]->(:Person{id:$id}) RETURN p")
    List<Person> findChildrenByParentId(String id);


}
