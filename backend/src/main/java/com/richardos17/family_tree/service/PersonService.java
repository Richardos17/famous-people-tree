package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.MarriageDTO;
import com.richardos17.family_tree.DTOs.ParentRelationshipDTO;
import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.PersonTreeResponseDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.domain.ExpandedPerson;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final FetchPerson fetchPerson;
    private final PersonSaveService personSaveService;
    private final PersonMapper personMapper;

    /**
     * Retrieves a list of Person objects by their name. The method first searches the local
     * database for persons matching the given name. If no matching records are found, it fetches
     * persons from an external source (e.g., Wikidata), saves them to the database if they do not
     * already exist, and returns the result.
     *
     * @param name the name of the person(s) to search for. It cannot be null or blank.
     * @return a list of Person objects matching the specified name. The list may be empty
     *         if no persons are found in both the database and external source.
     * @throws IllegalArgumentException if the provided name is null or blank.
     */
    public List<Person> getPersonsByName(String name) {
        //TODO: implement method to retrieve firstly persons from db and later add from wikidata
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        List<Person> personsDb = personRepository.searchByName(name);
        if (!personsDb.isEmpty()) {
            return personsDb;
        }
        return fetchPerson.fetchPersonsByName(name).stream()
                .map(person -> personRepository.findByWikidataId(person.getWikidataId())
                        .orElseGet(() -> {
                            person.setRelationshipsExpanded(false);
                            return personSaveService.savePerson(person);
                        }))
                .toList();
    }

    /**
     * Retrieves a Person entity by its Wikidata ID. This method first attempts to
     * find the Person in the repository. If the entity is not found locally, it
     * fetches the Person from an external source, saves it to the repository, and
     * returns the saved entity.
     *
     * @param wikidataId the unique identifier for the Person entity in Wikidata; must not be null or blank
     * @return an Optional containing the Person entity if found either locally or via external fetch;
     *         otherwise, an empty Optional if the Person could not be retrieved
     * @throws IllegalArgumentException if the provided wikidataId is null or blank
     */
    public Optional<Person> getPersonByWikidataId(String wikidataId) {
        if (wikidataId == null || wikidataId.isBlank()) {
            throw new IllegalArgumentException("Wikidata ID is required");
        }
        Optional<Person> personOptional = personRepository.findByWikidataId(wikidataId);
        if (personOptional.isPresent()) {
            return personOptional;
        }
        else {
            Person person = fetchPerson.fetchPersonByWikidataId(wikidataId);
            if (person == null) {
                return Optional.empty();
            }
            person.setRelationshipsExpanded(false);
            return Optional.of(personSaveService.savePerson(person));
        }
    }

    /**
     * Retrieves a person tree based on a provided Wikidata ID.
     *
     * @param wikidataId the unique identifier from Wikidata for the person
     * @param depth the depth of the tree to be retrieved; must be greater than 0
     * @param fullTree flag indicating whether to retrieve the full person tree
     * @return an Optional containing {@code PersonTreeResponseDTO} if the person tree is successfully retrieved, or an empty Optional otherwise
     * @throws IllegalArgumentException if the Wikidata ID is null, blank, or depth is less than 1
     * @throws UnsupportedOperationException if the full tree retrieval is requested and not implemented
     */
    public Optional<PersonTreeResponseDTO> getPersonTreeByWikidataId(String wikidataId, int depth, boolean fullTree) {
        if (wikidataId == null || wikidataId.isBlank()) {
            throw new IllegalArgumentException("Wikidata ID is required");
        }
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be greater than 0");
        }
        if (fullTree) {
            throw new UnsupportedOperationException("Full tree is not yet implemented");
        }
        return buildSimplePersonTree(wikidataId, depth);
    }

    private Optional<PersonTreeResponseDTO> buildSimplePersonTree(String wikidataId, int depth) {

        Optional<Person> personOptional = personRepository.findByWikidataId(wikidataId);
        Optional<List<FamilyRelationship>> childrenOptional = Optional.empty();

        if (personOptional.isEmpty() || !personOptional.get().getRelationshipsExpanded()) {
            ExpandedPerson fullPerson = fetchPerson.fetchFullPersonByWikidataId(wikidataId);
            if (fullPerson.person() == null) {
                return Optional.empty();
            }
            fullPerson.person().setRelationshipsExpanded(true);
            ExpandedPerson savedExpandedPerson = personSaveService.saveExpandedPerson(fullPerson);
            personOptional = Optional.of(savedExpandedPerson.person());
            childrenOptional = Optional.of(savedExpandedPerson.childRelationships());
        } else {
            personOptional.get().setSpouses(personRepository.findSpousesByPersonId(wikidataId));
        }
        Person person = personOptional.get();
        Set<RelationshipDTO> relationshipDTOS = new HashSet<>();
        Set<PersonDTO> persons = new HashSet<>();

        List<MarriedTo> spouses = person.getSpouses() != null ? person.getSpouses() : List.of();
        spouses.forEach(married -> relationshipDTOS.add(new MarriageDTO(
                person.getWikidataId(), married.getSpouse().getWikidataId(), married.getStartDate(), married.getEndDate())));
        persons.addAll(spouses.stream().map(married -> personMapper.toDTO(married.getSpouse())).toList());

        if (depth == 1) {
            persons.add(personMapper.toDTO(person));

            List<FamilyRelationship> directParents = person.getParents() != null
                    ? person.getParents()
                    : personRepository.findParentsByChildId(wikidataId);
            directParents.forEach(parentRel -> {
                relationshipDTOS.add(new ParentRelationshipDTO(wikidataId, parentRel.getEntity().getWikidataId()));
                persons.add(personMapper.toDTO(parentRel.getEntity()));
            });

            List<FamilyRelationship> directChildren = childrenOptional
                    .orElseGet(() -> personRepository.findChildrenByParentId(wikidataId));
            directChildren.forEach(childRel -> {
                relationshipDTOS.add(new ParentRelationshipDTO(childRel.getEntity().getWikidataId(), wikidataId));
                persons.add(personMapper.toDTO(childRel.getEntity()));
            });
        } else {
            traverseUpwards(person, depth, persons, relationshipDTOS);
            traverseDownwards(person, depth, persons, relationshipDTOS);
        }

        return Optional.of(new PersonTreeResponseDTO(persons, relationshipDTOS));
    }

    private void traverseUpwards(Person person, int depth, Set<PersonDTO> visited, Set<RelationshipDTO> relationships) {
        if (depth == 0) {
            visited.add(personMapper.toDTO(person));
            return;
        }
        if (visited.stream().anyMatch(dto -> dto.getWikidataId().equals(person.getWikidataId()))) {
            return;
        }
        visited.add(personMapper.toDTO(person));

        List<FamilyRelationship> parents;
        if (person.getRelationshipsExpanded() == null || !person.getRelationshipsExpanded()) {
            ExpandedPerson expandedPerson = fetchPerson.fetchFullPersonByWikidataId(person.getWikidataId());
            personSaveService.saveExpandedPerson(expandedPerson);
            parents = expandedPerson.person().getParents() != null ? expandedPerson.person().getParents() : List.of();
        } else {
            parents = personRepository.findParentsByChildId(person.getWikidataId());
        }
        parents.forEach(parentRel -> {
            relationships.add(new ParentRelationshipDTO(person.getWikidataId(), parentRel.getEntity().getWikidataId()));
            traverseUpwards(parentRel.getEntity(), depth - 1, visited, relationships);
        });
    }

    private void traverseDownwards(Person person, int depth, Set<PersonDTO> visited, Set<RelationshipDTO> relationships) {
        if (depth == 0) {
            visited.add(personMapper.toDTO(person));
            return;
        }
        if (visited.stream().anyMatch(dto -> dto.getWikidataId().equals(person.getWikidataId()))) {
            return;
        }
        visited.add(personMapper.toDTO(person));

        List<FamilyRelationship> children;
        if (person.getRelationshipsExpanded() == null || !person.getRelationshipsExpanded()) {
            ExpandedPerson expandedPerson = fetchPerson.fetchFullPersonByWikidataId(person.getWikidataId());
            personSaveService.saveExpandedPerson(expandedPerson);
            children = expandedPerson.childRelationships() != null ? expandedPerson.childRelationships() : List.of();
        } else {
            children = personRepository.findChildrenByParentId(person.getWikidataId());
        }
        children.forEach(childRel -> {
            relationships.add(new ParentRelationshipDTO(childRel.getEntity().getWikidataId(), person.getWikidataId()));
            traverseDownwards(childRel.getEntity(), depth - 1, visited, relationships);
        });
    }
}
