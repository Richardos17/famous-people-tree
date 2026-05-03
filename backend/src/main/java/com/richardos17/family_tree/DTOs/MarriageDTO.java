package com.richardos17.family_tree.DTOs;

import com.richardos17.family_tree.domain.Person;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MarriageDTO {
    private PersonDTO spouse;
    private LocalDate startDate;
    private LocalDate endDate;
}
