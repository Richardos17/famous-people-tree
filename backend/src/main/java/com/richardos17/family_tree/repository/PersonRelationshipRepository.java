package com.richardos17.family_tree.repository;

import com.richardos17.family_tree.domain.Country;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PersonRelationshipRepository {

    private final Neo4jClient neo4jClient;

    public List<FamilyRelationship> findParentsByChildId(String wikidataId) {
        return neo4jClient.query("""
                MATCH (parent:Person)<-[r:HAS_PARENT]-(:Person {wikidataId: $wikidataId})
                OPTIONAL MATCH (parent)-[:BORN_IN]-(country:Country)
                RETURN parent AS entity,
                       collect(country) AS bornIn
                """)
                .bind(wikidataId).to("wikidataId")
                .fetchAs(FamilyRelationship.class)
                .mappedBy((typeSystem, record) -> {
                    Person entity = mapPerson(record.get("entity").asNode());
                    mapBornIn(record, entity);
                    return new FamilyRelationship(null, entity);
                })
                .all()
                .stream().toList();
    }

    public List<FamilyRelationship> findChildrenByParentId(String wikidataId) {
        return neo4jClient.query("""
                MATCH (child:Person)-[r:HAS_PARENT]->(:Person {wikidataId: $wikidataId})
                OPTIONAL MATCH (child)-[:BORN_IN]-(country:Country)
                RETURN child AS entity,
                       collect(country) AS bornIn
                """)
                .bind(wikidataId).to("wikidataId")
                .fetchAs(FamilyRelationship.class)
                .mappedBy((typeSystem, record) -> {
                    Person entity = mapPerson(record.get("entity").asNode());
                    mapBornIn(record, entity);
                    return new FamilyRelationship(null, entity);
                })
                .all()
                .stream().toList();
    }

    public List<MarriedTo> findSpousesByPersonId(String wikidataId) {
        return neo4jClient.query("""
                MATCH (spouse:Person)<-[r:MARRIED_TO]-(:Person {wikidataId: $wikidataId})
                OPTIONAL MATCH (spouse)-[:BORN_IN]-(country:Country)
                RETURN spouse AS entity,
                       r.start_date AS startDate,
                       r.end_date AS endDate,
                       collect(country) AS bornIn
                """)
                .bind(wikidataId).to("wikidataId")
                .fetchAs(MarriedTo.class)
                .mappedBy((typeSystem, record) -> {
                    Person entity = mapPerson(record.get("entity").asNode());
                    LocalDate start = record.get("startDate").isNull() ? null :
                            record.get("startDate").asLocalDate();
                    LocalDate end = record.get("endDate").isNull() ? null :
                            record.get("endDate").asLocalDate();
                    mapBornIn(record, entity);
                    return new MarriedTo(null, entity, start, end);
                })
                .all()
                .stream().toList();
    }

    // --- private helpers ---

    private Person mapPerson(org.neo4j.driver.types.Node node) {
        return Person.builder()
                .localId(node.get("localId").asString(null))
                .wikidataId(node.get("wikidataId").asString(null))
                .name(node.get("name").asString(null))
                .birthdate(node.get("birthdate").isNull() ? null :
                        node.get("birthdate").asLocalDate())
                .deathdate(node.get("deathdate").isNull() ? null :
                        node.get("deathdate").asLocalDate())
                .wikipediaLink(node.get("wikipediaLink").asString(null))
                .imageLink(node.get("imageLink").asString(null))
                .relationshipsExpanded(node.get("relationships_expanded").asBoolean(false))

                .build();
    }

    private void mapBornIn(org.neo4j.driver.Record record, Person entity) {
        List<Object> bornInList = record.get("bornIn").asList();
        if (!bornInList.isEmpty()) {
            org.neo4j.driver.types.Node node =
                    (org.neo4j.driver.types.Node) bornInList.get(0);
            Country country = new Country(node.get("localId").asString(null),
                    node.get("wikidataId").asString(null), node.get("name").asString(null));
            entity.setBornIn(country);
        }
    }
}