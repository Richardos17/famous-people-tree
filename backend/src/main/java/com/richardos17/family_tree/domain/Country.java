package com.richardos17.family_tree.domain;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Country")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Country {

    @Id
    @GeneratedValue
    private String localId;

    private String wikidataId;

    private String name;
}
