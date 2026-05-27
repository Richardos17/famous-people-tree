package com.richardos17.family_tree.service;

import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@RequiredArgsConstructor
@Service
public class PersonQueryService {
    private final PersonRepository personRepository;

    /**
     * Load a basic Person (only person data, no relationships)
     */
    public Optional<Person> loadBasicPerson(String wikidataId) {
        return personRepository.findByWikidataId(wikidataId);
    }

    /**
     * Builds a simple tree recursively but in one direction per branch:
     * - Parents traverse only upwards (parents of parents, etc.)
     * - Children traverse only downwards (children of children, etc.)
     */
    public Optional<Person> buildSimplePersonTree(String id, int depth) {
        Optional<Person> person = loadBasicPerson(id);
        if (person.isEmpty()) {
            return Optional.empty();
        }
        var spouses = personRepository.findSpousesByPersonId(id);
        person.get().setSpouses(spouses);
        // Traverse parents upwards only

        traverseUpwards(person.get(), depth + 1, new HashSet<>());

        // Traverse children downwards only
        traverseDownwards(person.get(), depth + 1, new HashSet<>());

        return person;
    }

    /**
     * Builds a full tree traversing both parents and children recursively
     */
    public Person buildPersonTree(String id, int depth) {
        return buildFullTree(id, depth, new HashSet<>());
    }

    /**
     * Traverses upwards only (parents and their parents)
     */
    private void traverseUpwards(Person person, int depth, Set<String> visited) {
        if (depth == 0 || visited.contains(person.getWikidataId())) {
            return;
        }
        visited.add(person.getWikidataId());
        // Only traverse parents
        var parents = personRepository.findParentsByChildId(person.getWikidataId());
        if (!parents.isEmpty()) {
            person.setParents(parents.stream()
                    .peek(parentRel -> traverseUpwards(
                            parentRel.getEntity(),
                            depth - 1,
                            visited
                    ))
                    .toList());
        }
    }
    /**
     * Traverses downwards only (children and their children)
     */
    private void traverseDownwards(Person person, int depth, Set<String> visited) {
        if (depth == 0 || visited.contains(person.getWikidataId())) {
            return;
        }
        visited.add(person.getWikidataId());
        // Only traverse children
        var children = personRepository.findChildrenByParentId(person.getWikidataId());
        if (!children.isEmpty()) {
            children.stream()
                    .peek(childRel -> traverseDownwards(
                            childRel.getEntity(),
                            depth - 1,
                            visited
                    ))
                    .toList();
        }
    }

    /**
     * Traverses the full family tree in both directions recursively
     */
    private Person buildFullTree(String id, int depth, Set<String> visited) {
        Person person = loadBasicPerson(id).get();
        if (person == null) {
            return null;
        }

        // If depth is 0 or already visited, return person without relationships
        if (depth == 0 || visited.contains(id)) {
            return person;
        }

        visited.add(id);

        // Traverse parents recursively
        var parents = personRepository.findParentsByChildId(id);
        if (!parents.isEmpty()) {
            person.setParents(parents.stream()
                    .peek(parentRel -> parentRel.setEntity(buildFullTree(
                            parentRel.getEntity().getLocalId(),
                            depth - 1,
                            visited
                    )))
                    .toList());
        }

        // Traverse children recursively
        var children = personRepository.findChildrenByParentId(id);
//        if (!children.isEmpty()) {
//            person.setChildren(children.stream()
//                    .peek(childRel -> childRel.setEntity(buildFullTree(
//                            childRel.getEntity().getLocalId(),
//                            depth - 1,
//                            visited
//                    )))
//                    .toList());
//        }

        // Traverse spouses recursively
        var spouses = person.getSpouses();
        if (spouses != null && !spouses.isEmpty()) {
            person.setSpouses(spouses.stream()
                    .peek(marriedTo -> marriedTo.setSpouse(buildFullTree(
                            marriedTo.getSpouse().getLocalId(),
                            depth - 1,
                            visited
                    )))
                    .toList());
        }

        return person;
    }
}
