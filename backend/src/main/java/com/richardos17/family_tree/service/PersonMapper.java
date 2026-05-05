package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.CountryDTO;
import com.richardos17.family_tree.DTOs.MarriageDTO;
import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.domain.Country;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    public PersonDTO toDTO(Person person) {
        if (person == null) {
            return null;
        }

        PersonDTO dto = new PersonDTO();
        dto.setLocalId(person.getLocalId());
        dto.setWikidataId(person.getWikidataId());
        dto.setName(person.getName());
        dto.setBirthdate(person.getBirthdate());
        dto.setDeathdate(person.getDeathdate());
        dto.setImageLink(person.getImageLink());
        dto.setWikipediaLink(person.getWikipediaLink());
        dto.setHeight(person.getHeight());

        if (person.getBornIn() != null) {
            dto.setBornIn(countryToDTO(person.getBornIn()));
        }

        if (person.getSpouses() != null) {
            dto.setSpouses(person.getSpouses().stream()
                    .map(this::marriageToDTO)
                    .toList());
        }
        if (person.getParents() != null) {
            dto.setParents(person.getParents().stream()
                    .map(this::relationshipToDTO)
                    .toList());
        }
        if (person.getChildren() != null) {
            dto.setChildren(person.getChildren().stream()
                    .map(this::relationshipToDTO)
                    .toList());
        }


        return dto;
    }

    public CountryDTO countryToDTO(Country country) {
        if (country == null) {
            return null;
        }
        return new CountryDTO(country.getName());
    }

    public MarriageDTO marriageToDTO(MarriedTo marriage) {
        if (marriage == null) {
            return null;
        }
        MarriageDTO dto = new MarriageDTO();
        dto.setSpouse(toDTO(marriage.getSpouse()));
        dto.setStartDate(marriage.getStartDate());
        dto.setEndDate(marriage.getEndDate());
        return dto;
    }
    public MarriageDTO marriageToDTO(MarriedTo marriage, PersonDTO mappedEntity) {
        if (marriage == null) {
            return null;
        }
        return new MarriageDTO(mappedEntity, marriage.getStartDate(), marriage.getEndDate());

    }
    public RelationshipDTO relationshipToDTO(FamilyRelationship relationship, PersonDTO mappedEntity) {
        if (relationship == null) {
            return null;
        }

        return new RelationshipDTO(mappedEntity, relationship.getType());
    }
    public RelationshipDTO relationshipToDTO(FamilyRelationship relationship) {
        if (relationship == null) {
            return null;
        }
        return new RelationshipDTO(toDTO(relationship.getEntity()), relationship.getType());
    }
}
