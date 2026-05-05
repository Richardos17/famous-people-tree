package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.CountryDTO;
import com.richardos17.family_tree.DTOs.MarriageDTO;
import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.Country;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.CountryRepository;
import com.richardos17.family_tree.repository.PersonRepository;
import com.richardos17.family_tree.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
@Transactional
public class PersonCreateService {
    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;
    private final IdGenerator idGenerator;

    public Person createPerson(PersonDTO personDTO) {
        Person person = Person.builder()
                .name(personDTO.getName())
                .birthdate(personDTO.getBirthdate())
                .deathdate(personDTO.getDeathdate())
                .imageLink(personDTO.getImageLink())
                .wikipediaLink(personDTO.getWikipediaLink())
                .height(personDTO.getHeight())
                .localId(idGenerator.generateUniquePersonId())
                .wikidataId(personDTO.getWikidataId())
                .build();

        if (personDTO.getBornIn() != null) {
            person.setBornIn(resolveCountry(personDTO.getBornIn()));
        }

        return personRepository.save(person);
    }

    public Person addBornIn(String personId, CountryDTO bornInDTO) {
        if (bornInDTO == null || bornInDTO.getName() == null || bornInDTO.getName().isBlank()) {
            throw new IllegalArgumentException("Country name is required");
        }

        Person person = findPersonOrThrow(personId);
        person.setBornIn(resolveCountry(bornInDTO));
        return personRepository.save(person);
    }

    public Person addRelationship(String childId, RelationshipDTO relationshipDTO) {
        if (relationshipDTO == null || relationshipDTO.getPerson() == null || relationshipDTO.getPerson().getLocalId() == null) {
            throw new IllegalArgumentException("Relationship person is required");
        }

        Person childPerson = findPersonOrThrow(childId);
        Person relatedPerson = findPersonOrThrow(relationshipDTO.getPerson().getLocalId());

        if (childPerson.getParents() == null) {
            childPerson.setParents(new ArrayList<>());
        }

        childPerson.getParents().add(new FamilyRelationship(null, relatedPerson, relationshipDTO.getType()));
        return personRepository.save(childPerson);
    }

    public Person addMarriage(String personId, MarriageDTO marriageDTO) {
        if (marriageDTO == null || marriageDTO.getSpouse() == null || marriageDTO.getSpouse().getLocalId() == null) {
            throw new IllegalArgumentException("Marriage spouse is required");
        }

        Person person = findPersonOrThrow(personId);
        Person spouse = findPersonOrThrow(marriageDTO.getSpouse().getLocalId());

        if (person.getSpouses() == null) {
            person.setSpouses(new ArrayList<>());
        }

        person.getSpouses().add(new MarriedTo(null, spouse, marriageDTO.getStartDate(), marriageDTO.getEndDate()));
        return personRepository.save(person);
    }

    private Country resolveCountry(CountryDTO countryDTO) {
        String countryName = countryDTO.getName();
        return countryRepository.findByName(countryName)
                .orElseGet(() -> countryRepository.save(new Country(null, null, countryName)));
    }

    private Person findPersonOrThrow(String localId) {
        if (localId == null || localId.isBlank()) {
            throw new IllegalArgumentException("Person localId is required");
        }

        return personRepository.findByLocalId(localId)
                .orElseThrow(() -> new NoSuchElementException("Person not found: " + localId));
    }
}
