package com.richardos17.family_tree.entities;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Person")
public class Person {

    @Id
    private String id;

    private String name;
    private String birthdate;
    private String deathdate;
    private String imageLink;
    private String wikipediaLink;
    private Integer height;

    // getters/setters
}
