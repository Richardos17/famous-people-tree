package com.richardos17.family_tree.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Date;
import java.util.List;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING;
import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Person")
public class Person {

    @Id
    private String id;

    private String name;
    private Date birthdate;
    private Date deathdate;
    private String imageLink;
    private String wikipediaLink;
    private Integer height;

    @Relationship(type = "HasParent", direction = OUTGOING)
    private List<Person> parents;

    @Relationship(type = "HasParent", direction = INCOMING)
    private List<Person> children;

    @Relationship(type = "MarriedTo", direction = INCOMING)
    private List<Person> spouses;

    @Relationship(direction = OUTGOING)
    private Country bornIn;

    // getters/setters
}
