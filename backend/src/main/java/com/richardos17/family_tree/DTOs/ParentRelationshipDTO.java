package com.richardos17.family_tree.DTOs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ParentRelationshipDTO extends RelationshipDTO {

    public ParentRelationshipDTO(String personFromWikidataId, String persontoWikidataId) {
        super(personFromWikidataId, persontoWikidataId, "parent");
    }
}
