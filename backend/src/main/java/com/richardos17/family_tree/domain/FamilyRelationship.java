package com.richardos17.family_tree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
@RelationshipProperties
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FamilyRelationship {
    @Id
    @GeneratedValue
    private String id;
    @TargetNode
    private Person entity;
}
