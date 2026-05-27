package com.richardos17.family_tree.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
@Data
@AllArgsConstructor
public class PersonTreeResponseDTO {
    private Set<PersonDTO> persons;
    private Set<RelationshipDTO> relationships;
}
