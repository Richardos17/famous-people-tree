package com.richardos17.family_tree.domain;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDate;


@RelationshipProperties
@Data
@AllArgsConstructor
public class MarriedTo {
    @Id
    @GeneratedValue
    private String id;

    @TargetNode
    private Person spouse;
    @Property("start_date")
    private LocalDate startDate;
    @Property("end_date")
    private LocalDate endDate;
}
