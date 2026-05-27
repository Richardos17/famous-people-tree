package com.richardos17.family_tree.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class RelationshipDTO {
    private String personFromWikidataId;
    private String persontoWikidataId;
    private String type;
}
