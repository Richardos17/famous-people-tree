package com.richardos17.family_tree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Date;
import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Person")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Person {

    @Id
    @GeneratedValue
    private String id;

    private String wikidataId;
    private String name;
    private Date birthdate;
    private Date deathdate;
    private String imageLink;
    private String wikipediaLink;
    private Integer height;

    @Relationship(type = "HasParent")
    private List<Person> parents;

    @Relationship(type = "MarriedTo")
    private List<Person> spouses;

    @Relationship(type = "BornIn", direction = OUTGOING)
    private Country bornIn;
}
