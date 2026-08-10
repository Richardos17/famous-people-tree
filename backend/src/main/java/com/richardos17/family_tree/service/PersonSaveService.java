package com.richardos17.family_tree.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import com.richardos17.family_tree.domain.ExpandedPerson;
import com.richardos17.family_tree.domain.FamilyRelationship;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.richardos17.family_tree.domain.Country;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.CountryRepository;
import com.richardos17.family_tree.repository.PersonRepository;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class PersonSaveService {
    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;

    /**
     * Saves person entity to the database. If the person's country is not already in the database, it will be added.
     * If the person's country is already in the database, it will be retrieved from the database.
     * If a concurrent request inserts the same wikidataId first, the existing entity is returned instead of failing.
     * @param person The person entity to save
     * @return The saved person entity with valid localId.
     */
    public Person savePerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (person.getWikidataId() == null || person.getWikidataId().isBlank()) {
            throw new IllegalArgumentException("Person Wikidata ID is required");
        }
        resolveCountry(person);
        personRepository.findByWikidataId(person.getWikidataId())
                .ifPresent(existing -> {
                    if (person.getLocalId() != null && !person.getLocalId().equals(existing.getLocalId())) {
                        throw new IllegalArgumentException("Person with the same wikidataId already exists with a different localId");
                    }
                    person.setLocalId(existing.getLocalId());

                    if (existing.getRelationshipsExpanded()) {
                        person.setRelationshipsExpanded(true);
                    }
                });

        return personRepository.save(person);
    }

    /**
     * Saves expanded person entity to the database. If the person's country is not already in the database, it will be added.
     * If the person's country is already in the database, it will be retrieved from the database.
     * Relationship expanded is set to true.
     * @param expandedPerson The expanded person entity to save.
     * @return The saved expanded person entity with valid localId and all entities with valid localId.
     * @throws IllegalArgumentException If the expanded person, person, or person's wikidataId is null or blank.
     */
    public void saveExpandedPerson(ExpandedPerson expandedPerson) {
        if (expandedPerson == null) {
            throw new IllegalArgumentException("Expanded person is null");
        }
        if (expandedPerson.spouses() == null) {
            throw new IllegalArgumentException("Spouses are required");
        }
        if (expandedPerson.parentRelationships() == null) {
            throw new IllegalArgumentException("Parents are required");
        }
        if (expandedPerson.childRelationships() == null) {
            throw new IllegalArgumentException("Children are required");
        }
        if (expandedPerson.person() == null) {
            throw new IllegalArgumentException("Person is required");
        }
        expandedPerson.person().setRelationshipsExpanded(true);
        Person savedPerson = savePerson(expandedPerson.person());
        //TODO optimize by saving in bulk all persons and relationships
        expandedPerson.spouses().forEach(marriedTo -> savePerson(marriedTo.getSpouse()));

        List<String> parentIds = new ArrayList<>();
        expandedPerson.parentRelationships().forEach(familyRelationship ->
                parentIds.add(savePerson(familyRelationship.getEntity()).getLocalId()));

        List<String> childIds = new ArrayList<>();
        expandedPerson.childRelationships().forEach(familyRelationship ->
                childIds.add(savePerson(familyRelationship.getEntity()).getLocalId()));

        parentIds.forEach(parentId -> personRepository.createParentRelationship(parentId, savedPerson.getLocalId()));
        childIds.forEach(childId -> personRepository.createParentRelationship(savedPerson.getLocalId(), childId));

    }
    public void saveFamilyRelationship(String parentLocalId, String childLocalId) {
        if (parentLocalId == null || parentLocalId.isBlank() || childLocalId == null || childLocalId.isBlank()) {
            throw new IllegalArgumentException("Parent and child localIds are required");
        }
        personRepository.createParentRelationship(parentLocalId, childLocalId);
    }
    public void saveMarriedRelationship(String spouse1LocalId, String spouse2LocalId, LocalDate startDate, LocalDate endDate) {
        if (spouse1LocalId == null || spouse1LocalId.isBlank() || spouse2LocalId == null || spouse2LocalId.isBlank()) {
            throw new IllegalArgumentException("Spouses localIds are required");
        }
        personRepository.createMarriageRelationship(spouse1LocalId, spouse2LocalId, startDate, endDate);
    }


    /**
     * Resolves the country associated with the given person. If the person's country does not exist in the database,
     * it is created and saved. If the country already exists, the existing country entity is used.
     * If a concurrent request inserts the same country first, the existing entity is returned instead of failing.
     *
     * @param person The person entity whose associated country needs to be resolved.
     * @throws IllegalArgumentException If the country wikidataId is null or blank, or the country name is null or blank when a new country needs to be created.
     */
    private void resolveCountry(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (person.getBornIn() != null && person.getBornIn().getLocalId() == null) {
            Country country = person.getBornIn();
            if (country.getWikidataId() == null || country.getWikidataId().isBlank()) {
                throw new IllegalArgumentException("Country Wikidata ID is required");
            }
            if (country.getName() == null || country.getName().isBlank()) {
                throw new IllegalArgumentException("Country name is required");
            }
            if (countryRepository.existsByWikidataId(country.getWikidataId())) {
                person.setBornIn(countryRepository.getCountryByWikidataId(country.getWikidataId()));
                return;
            }
            person.setBornIn(countryRepository.mergeByWikidataId(country.getWikidataId(), country.getName()));
        }
    }
}
