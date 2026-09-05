package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.CountryDTO;
import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.domain.Country;

import com.richardos17.family_tree.domain.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {
    /**
     * Converts a Person entity to its corresponding PersonDTO. Relatives are not included in the DTO.
     *
     * @param person the Person entity to be converted. If null, the method returns null.
     * @return the converted PersonDTO instance. If the input Person is null, returns null.
     * @throws IllegalArgumentException if the input Person is null, or if its wikidataId is null or empty.
     */
    public PersonDTO toDTO(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null");
        }
        if (person.getWikidataId() == null || person.getWikidataId().isBlank()) {
            throw new IllegalArgumentException("Person Wikidata ID cannot be null or empty");
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
        return dto;
    }

    /**
     * Converts a Country entity to its corresponding CountryDTO.
     *
     * @param country the Country entity to be converted. Must not be null, and its name and wikidataId must not be null or empty.
     * @return the converted CountryDTO instance containing the name and wikidataId of the Country entity.
     * @throws IllegalArgumentException if the input Country is null, or if its name or wikidataId is null or empty.
     */
    public CountryDTO countryToDTO(Country country) {
        if (country == null) {
            throw new IllegalArgumentException("Country cannot be null");
        }
        if (country.getWikidataId() == null || country.getWikidataId().isBlank()) {
            throw new IllegalArgumentException("Country Wikidata ID cannot be null or empty");
        }
        if (country.getName() == null || country.getName().isBlank()) {
            throw new IllegalArgumentException("Country name cannot be null or empty");
        }
        return new CountryDTO(country.getName(), country.getWikidataId());
    }
}
