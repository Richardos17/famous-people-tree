package com.richardos17.family_tree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FamilyRelationship<T> {
    private T entity;
    private String type;
}
