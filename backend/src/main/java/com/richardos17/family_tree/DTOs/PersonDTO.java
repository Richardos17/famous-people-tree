package com.richardos17.family_tree.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PersonDTO {
    private String localId;
    private String wikidataId;
    private String name;
    private LocalDate birthdate;
    private LocalDate deathdate;
    private String imageLink;
    private String wikipediaLink;
    private Integer height;

    private List<MarriageDTO> spouses;

    private CountryDTO bornIn;

    private List<RelationshipDTO> parents;

    private List<RelationshipDTO> children;
}
