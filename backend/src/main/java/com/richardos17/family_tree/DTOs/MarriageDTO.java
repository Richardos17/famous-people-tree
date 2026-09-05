package com.richardos17.family_tree.DTOs;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MarriageDTO extends RelationshipDTO {
    private LocalDate startDate;
    private LocalDate endDate;

    public MarriageDTO(String personFromWikidataId, String persontoWikidataId, LocalDate startDate, LocalDate endDate) {
        super(personFromWikidataId, persontoWikidataId, "spouse");
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
