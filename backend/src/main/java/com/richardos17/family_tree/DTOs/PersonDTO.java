package com.richardos17.family_tree.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "wikidataId")
public class PersonDTO {
    private String localId;
    private String wikidataId;
    private String name;
    private LocalDate birthdate;
    private LocalDate deathdate;
    private String imageLink;
    private String wikipediaLink;
    private Integer height;


    private CountryDTO bornIn;

}
