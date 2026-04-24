package com.richardos17.family_tree.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Country")
public class Country {

    @Id
    private String id;

    private String name;
}
