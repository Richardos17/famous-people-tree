package com.richardos17.family_tree.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Person")
@NoArgsConstructor
@Getter
@Setter
public class Person {
    @Id
    private String localId;
    private String wikidataId;
    private String name;
    private LocalDate birthdate;
    private LocalDate deathdate;
    private String imageLink;
    private String wikipediaLink;
    private Integer height;

    @Relationship(type = "MARRIED_TO")
    private List<MarriedTo> spouses;

    @Relationship(type = "BORN_IN", direction = OUTGOING)
    private Country bornIn;
    @Relationship(type = "HAS_PARENT", direction = OUTGOING)
    private List<FamilyRelationship> parents;

    @Transient
    private List<FamilyRelationship> children;

    public Person(Person person) {
        this.localId = person.localId;
        this.wikidataId = person.wikidataId;
        this.name = person.name;
        this.birthdate = person.birthdate;
        this.deathdate = person.deathdate;
        this.imageLink = person.imageLink;
        this.wikipediaLink = person.wikipediaLink;
        this.height = person.height;
        this.spouses = person.spouses;
        this.bornIn = person.bornIn;
        this.parents = person.parents;
        this.children = person.children;
    }
}
