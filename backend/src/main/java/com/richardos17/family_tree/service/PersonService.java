package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.MarriageDTO;
import com.richardos17.family_tree.DTOs.ParentRelationshipDTO;
import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.PersonTreeResponseDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRelationshipRepository;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final FetchPerson fetchPerson;
    private final PersonSaveService personSaveService;
    private final PersonMapper personMapper;
    private final PersonRelationshipRepository personRelationshipRepository;
    private final int expandedDecidingDepth = 2;


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
                .map(person -> {
                            person.setRelationshipsExpanded(false);
                            return personSaveService.savePerson(person);
                        })
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
        Optional<Person> personOptional = getPersonByWikidataId(wikidataId);

        if (personOptional.isEmpty()) {
            try {
                personOptional = Optional.of(fetchPerson.fetchPersonByWikidataId(wikidataId));
            }
            catch (NoSuchElementException e) {
                return Optional.empty();
            }
        }
        Person person = personOptional.get();
        Set<RelationshipDTO> relationshipDTOS = new HashSet<>();
        Set<PersonDTO> persons = new HashSet<>();

        List<MarriedTo> spouses;
        if (person.getRelationshipsExpanded()) {
            spouses = personRelationshipRepository.findSpousesByPersonId(person.getWikidataId());
        }
        else {
            spouses = fetchPerson.fetchSpousesByWikidataId(person.getWikidataId());
            spouses.forEach(marriedTo -> {
                marriedTo.setSpouse(personSaveService.savePerson(marriedTo.getSpouse()));
                personSaveService.saveMarriedRelationship(person.getLocalId(), marriedTo.getSpouse().getLocalId(), marriedTo.getStartDate(), marriedTo.getEndDate());
            });
        }
        spouses.forEach(married -> relationshipDTOS.add(new MarriageDTO(
                person.getWikidataId(), married.getSpouse().getWikidataId(), married.getStartDate(), married.getEndDate())));
        persons.addAll(spouses.stream().map(married -> personMapper.toDTO(married.getSpouse())).toList());

        Set<PersonDTO> personsUp = new HashSet<>();
        Set<PersonDTO> personsDown = new HashSet<>();
        boolean previouslyExpanded = person.getRelationshipsExpanded();
        traverseUpwards(person, depth, personsUp, relationshipDTOS);
        if (!previouslyExpanded) {
            person.setRelationshipsExpanded(false);
        }
        traverseDownwards(person, depth, personsDown, relationshipDTOS);
        personSaveService.savePerson(person);
        persons.addAll(personsUp);
        persons.addAll(personsDown);
        return Optional.of(new PersonTreeResponseDTO(persons, relationshipDTOS));
    }

    private void traverseUpwards(Person person, int depth, Set<PersonDTO> visited, Set<RelationshipDTO> relationships) {
        if (!visited.add(personMapper.toDTO(person))) { //TODO maybe change to set of ids, for faster look up
            return;
        }
        if (depth == 0) {
            return;
        }

        List<FamilyRelationship> parents;
        if (person.getRelationshipsExpanded()) {
            parents = personRelationshipRepository.findParentsByChildId(person.getWikidataId());

        } else {
            parents = fetchPerson.fetchParentsByWikidataId(person.getWikidataId());
            parents.forEach(parentRelationship -> {
                parentRelationship.setEntity(personSaveService.savePerson(parentRelationship.getEntity()));
                personSaveService.saveFamilyRelationship(parentRelationship.getEntity().getLocalId(), person.getLocalId());
            });

        }
        parents.forEach(parentRel -> {
            relationships.add(new ParentRelationshipDTO(person.getWikidataId(), parentRel.getEntity().getWikidataId()));
            traverseUpwards(parentRel.getEntity(), depth - 1, visited, relationships);
        });
        person.setRelationshipsExpanded(true);
        personSaveService.savePerson(person);
        //TODO set relationship expanded at the right time because when too early another leg stops to divide
    }

    private void traverseDownwards(Person person, int depth, Set<PersonDTO> visited, Set<RelationshipDTO> relationships) {
        if (!visited.add(personMapper.toDTO(person))) {
            return;
        }
        if (depth == 0) {
            return;
        }


        List<FamilyRelationship> children;
        if (person.getRelationshipsExpanded()) {
            children = personRelationshipRepository.findChildrenByParentId(person.getWikidataId());
        } else {
            children = fetchPerson.fetchChildrenByWikidataId(person.getWikidataId());
            children.forEach(childRelationship -> {
                childRelationship.setEntity(personSaveService.savePerson(childRelationship.getEntity()));
                personSaveService.saveFamilyRelationship(person.getLocalId(), childRelationship.getEntity().getLocalId());
            });
        }
        children.forEach(childRel -> {
            relationships.add(new ParentRelationshipDTO(childRel.getEntity().getWikidataId(), person.getWikidataId()));
            traverseDownwards(childRel.getEntity(), depth - 1, visited, relationships);
        });
        person.setRelationshipsExpanded(true);
        personSaveService.savePerson(person);
    }
//    private Person buildFullTree(String id, int depth, Set<String> visited) {
//        Person person = loadBasicPerson(id).get();
//        if (person == null) {
//            return null;
//        }
//
//        // If depth is 0 or already visited, return person without relationships
//        if (depth == 0 || visited.contains(id)) {
//            return person;
//        }
//
//        visited.add(id);
//
//        // Traverse parents recursively
//        var parents = personRepository.findParentsByChildId(id);
//        if (!parents.isEmpty()) {
//            person.setParents(parents.stream()
//                    .peek(parentRel -> parentRel.setEntity(buildFullTree(
//                            parentRel.getEntity().getLocalId(),
//                            depth - 1,
//                            visited
//                    )))
//                    .toList());
//        }
//
//        // Traverse children recursively
//        var children = personRepository.findChildrenByParentId(id);
////        if (!children.isEmpty()) {
////            person.setChildren(children.stream()
////                    .peek(childRel -> childRel.setEntity(buildFullTree(
////                            childRel.getEntity().getLocalId(),
////                            depth - 1,
////                            visited
////                    )))
////                    .toList());
////        }
//
//        // Traverse spouses recursively
//        var spouses = person.getSpouses();
//        if (spouses != null && !spouses.isEmpty()) {
//            person.setSpouses(spouses.stream()
//                    .peek(marriedTo -> marriedTo.setSpouse(buildFullTree(
//                            marriedTo.getSpouse().getLocalId(),
//                            depth - 1,
//                            visited
//                    )))
//                    .toList());
//        }
//
//        return person;
//    }
}
