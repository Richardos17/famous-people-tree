package com.richardos17.family_tree.domain;

import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.util.Date;

@RelationshipProperties
public class MarriedTo {

    @TargetNode
    private Person spouse;
    private Date startDate;
    private Date endDate;
}
