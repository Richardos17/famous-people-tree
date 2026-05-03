package com.richardos17.family_tree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChildWithRelationship {
    private String id;
    private Person child;
    private String type;
}
