package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.domain.FamilyRelationship;
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

        return dto;
    }

    public <T> RelationshipDTO<T> relationshipToDTO(FamilyRelationship<?> relationship, T mappedEntity) {
        if (relationship == null) {
            return null;
        }

        return new RelationshipDTO<>(mappedEntity, relationship.getType());
    }
}
