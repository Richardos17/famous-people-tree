package com.richardos17.family_tree.service;

import java.util.List;
import java.util.Optional;


import com.richardos17.family_tree.domain.ExpandedPerson;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
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
        resolveCountry(person);
        person.setRelationshipsExpanded(false);

        Optional<Person> existing = personRepository.findByWikidataId(person.getWikidataId());
        if (existing.isPresent()) {
            return existing.get();
        }
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
    public ExpandedPerson saveExpandedPerson(ExpandedPerson expandedPerson) {
        if (expandedPerson == null) {
            throw new IllegalArgumentException("Expanded person is null");
        }
        if (expandedPerson.person() == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (expandedPerson.person().getWikidataId() == null || expandedPerson.person().getWikidataId().isBlank()) {
            throw new IllegalArgumentException("Person Wikidata ID is required");
        }
        resolveSpouses(expandedPerson.person());
        resolveParents(expandedPerson.person());
        resolveChildren(expandedPerson.person(), expandedPerson.childRelationships());
        resolveCountry(expandedPerson.person());
        expandedPerson.person().setRelationshipsExpanded(true);
        return new ExpandedPerson(personRepository.save(expandedPerson.person()), expandedPerson.childRelationships());
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
            person.setBornIn(countryRepository.mergeByWikidataId(country.getWikidataId(), country.getName()));
        }
    }

    /**
     * If a person's spouses are not already in the database, they will be added. If they are already in the database,
     * they will be retrieved from the database. And their actual entities will be retrieved and the person gets entities with valid localId.
     * @param person The person entity to resolve spouses for.
     * @throws IllegalArgumentException If a spouse is missing or has a null or blank Wikidata ID.
     */
    private void resolveSpouses(Person person) {
        if (person.getSpouses() != null) {
            for (MarriedTo marriedTo : person.getSpouses()) {
                if (marriedTo.getSpouse() == null) {
                    throw new IllegalArgumentException("Spouse is required");
                }
                if (marriedTo.getSpouse().getWikidataId() == null || marriedTo.getSpouse().getWikidataId().isBlank()) {
                    throw new IllegalArgumentException("Spouse Wikidata ID is required");
                }
                Optional<Person> spouseOptional = personRepository.findByWikidataId(marriedTo.getSpouse().getWikidataId());
                if (spouseOptional.isPresent()) {
                    marriedTo.setSpouse(spouseOptional.get());
                } else {
                    marriedTo.setSpouse(savePerson(marriedTo.getSpouse()));
                }
            }
        }
    }
    /**
     * If a person's parents are not already in the database, they will be added. If they are already in the database,
     * they will be retrieved from the database. And their actual entities will be retrieved and the person gets entities with valid localId.
     * @param person The person entity to resolve parents for.
     * @throws IllegalArgumentException If a parent is missing or has a null or blank Wikidata ID.
     */
    private void resolveParents(Person person) {
        if (person.getParents() != null) {
            for (FamilyRelationship parentRelationship : person.getParents()) {
                if (parentRelationship.getEntity() == null) {
                    throw new IllegalArgumentException("Parent is required");
                }
                if (parentRelationship.getEntity().getWikidataId() == null || parentRelationship.getEntity().getWikidataId().isBlank()) {
                    throw new IllegalArgumentException("Parent Wikidata ID is required");
                }
                Optional<Person> parentOptional = personRepository.findByWikidataId(parentRelationship.getEntity().getWikidataId());
                if (parentOptional.isPresent()) {
                    parentRelationship.setEntity(parentOptional.get());
                } else {
                    parentRelationship.setEntity(savePerson(parentRelationship.getEntity()));
                }
            }
        }
    }

    /**
     * If a person's child is not already in the database, it will be added with the expanded person set as a parent. If it is already in the database, it will be retrieved from the database,
     * parent is set, and it will be saved back to the database.
     * And its actual entity will be retrieved, and the person gets the entity with valid localId of child.
     * @param parent The parent person entity to resolve children for.
     * @param childRelationships The list of child relationships to resolve.
     * @throws IllegalArgumentException If a child is missing or has a null or blank Wikidata ID.
     */
    private void resolveChildren(Person parent, List<FamilyRelationship> childRelationships) {
        if (childRelationships != null) {
            for (FamilyRelationship childRelationship : childRelationships) {
                if (childRelationship.getEntity() == null) {
                    throw new IllegalArgumentException("Child is required");
                }
                if (childRelationship.getEntity().getWikidataId() == null || childRelationship.getEntity().getWikidataId().isBlank()) {
                    throw new IllegalArgumentException("Child Wikidata ID is required");
                }
                Optional<Person> childOptional = personRepository.findByWikidataId(childRelationship.getEntity().getWikidataId());
                if (childOptional.isPresent()) {
                    childOptional.get().addParent(parent);
                    childRelationship.setEntity(savePerson(childOptional.get()));
                } else {
                    childRelationship.getEntity().addParent(parent);
                    childRelationship.setEntity(savePerson(childRelationship.getEntity()));
                }
            }
        }
    }
}
