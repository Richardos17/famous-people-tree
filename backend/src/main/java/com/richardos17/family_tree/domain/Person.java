package com.richardos17.family_tree.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.LocalDate;

import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Node("Person")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Person {
    @GeneratedValue(UUIDStringGenerator.class)
    @Id
    private String localId;
    private String wikidataId;
    private String name;
    private LocalDate birthdate;
    private LocalDate deathdate;
    @Property("image_link")
    private String imageLink;
    @Property("wikipedia_link")
    private String wikipediaLink;
    private Integer height;

    @Builder.Default
    @Property("relationships_expanded")
    private Boolean relationshipsExpanded = false;


    @Relationship(type = "BORN_IN", direction = OUTGOING)
    private Country bornIn;
}
