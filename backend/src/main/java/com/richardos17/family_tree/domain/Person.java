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
import java.util.ArrayList;
import java.util.List;

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
    @Property("relationships_expanded")
    private Boolean relationshipsExpanded = false;

    @Relationship(type = "MARRIED_TO")
    private List<MarriedTo> spouses;

    @Relationship(type = "BORN_IN", direction = OUTGOING)
    private Country bornIn;
    @Relationship(type = "HAS_PARENT", direction = OUTGOING)
    private List<FamilyRelationship> parents;


    public void addParent(Person parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is required");
        }
        if (parents == null) {
            parents = new ArrayList<>();
        }
        parents.add(new FamilyRelationship(null, parent));
    }

    public void addMarriage(Person spouse, LocalDate startDate, LocalDate endDate) {
        if (spouse == null) {
            throw new IllegalArgumentException("Spouse is required");
        }

        if (spouses == null) {
            spouses = new ArrayList<>();
        }

        spouses.add(new MarriedTo(null, spouse, startDate, endDate));
    }
}
