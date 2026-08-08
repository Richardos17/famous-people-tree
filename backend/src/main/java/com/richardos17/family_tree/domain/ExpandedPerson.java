package com.richardos17.family_tree.domain;

import java.util.List;

public record ExpandedPerson(Person person,
                             List<FamilyRelationship> parentRelationships,
                             List<FamilyRelationship> childRelationships,
                             List<MarriedTo> spouses
                             ) {

}
