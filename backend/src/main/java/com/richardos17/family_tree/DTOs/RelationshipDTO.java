package com.richardos17.family_tree.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RelationshipDTO {
    private PersonDTO person;
    private String type;
}
