package com.richardos17.family_tree.domain;

import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class HasParent {

    @TargetNode
    private Person parent;

    private String type;
}
