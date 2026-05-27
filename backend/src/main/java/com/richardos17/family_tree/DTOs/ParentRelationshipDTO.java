package com.richardos17.family_tree.DTOs;


import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ParentRelationshipDTO extends RelationshipDTO {

    public ParentRelationshipDTO(String personFromWikidataId, String persontoWikidataId) {
        super(personFromWikidataId, persontoWikidataId, "parent");
    }
}
