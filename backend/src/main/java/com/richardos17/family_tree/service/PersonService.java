package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@RequiredArgsConstructor
@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
       public Person combinePerson(Person person) {
        List<FamilyRelationship> children = personRepository.findChildrenByParentId(person.getLocalId());
        Person personCopy = new Person(person);
        personCopy.setChildren(children);
        return personCopy;
    }
    /**
     * Builds a simple tree recursively but in one direction per branch:
     * - Parents traverse only upwards (parents of parents, etc.)
     * - Children traverse only downwards (children of children, etc.)
     */
    public PersonDTO buildSimplePersonTree(String id, int depth) {
        Person person = personRepository.findByLocalId(id).orElse(null);
        if (person == null) {
            return null;
        }
        person = combinePerson(person);
        PersonDTO dto = personMapper.toDTO(person);

        // Traverse parents upwards only
        if (person.getParents() != null) {
            dto.setParents(person.getParents().stream()
                    .map(parentRel -> {
                        PersonDTO parentDTO = traverseUpwards(
                                parentRel.getEntity().getLocalId(),
                                depth - 1,
                                new HashSet<>()
                        );
                        return personMapper.relationshipToDTO(parentRel, parentDTO);
                    })
                    .toList());
        }

        // Traverse children downwards only
        if (person.getChildren() != null) {
            dto.setChildren(person.getChildren().stream()
                    .map(childRel -> {
                        PersonDTO childDTO = traverseDownwards(
                                childRel.getEntity().getLocalId(),
                                depth - 1,
                                new HashSet<>()
                        );
                        return personMapper.relationshipToDTO(childRel, childDTO);
                    })
                    .toList());
        }

        return dto;
    }

    /**
     * Builds a full tree traversing both parents and children recursively
     */
    public PersonDTO buildPersonTree(String id, int depth) {
        return buildFullTree(id, depth, new HashSet<>());
    }

    /**
     * Traverses upwards only (parents and their parents)
     */
    private PersonDTO traverseUpwards(String id, int depth, Set<String> visited) {
        if (depth == 0 || visited.contains(id)) {
            return null;
        }

        visited.add(id);

        Person person = personRepository.findByLocalId(id).orElse(null);
        if (person == null) {
            return null;
        }

        PersonDTO dto = personMapper.toDTO(person);

        // Only traverse parents
        if (person.getParents() != null) {
            dto.setParents(person.getParents().stream()
                    .map(parentRel -> {
                        PersonDTO parentDTO = traverseUpwards(
                                parentRel.getEntity().getLocalId(),
                                depth - 1,
                                visited
                        );
                        return personMapper.relationshipToDTO(parentRel, parentDTO);
                    })
                    .toList());
        }

        return dto;
    }

    /**
     * Traverses downwards only (children and their children)
     */
    private PersonDTO traverseDownwards(String id, int depth, Set<String> visited) {
        if (depth == 0 || visited.contains(id)) {
            return null;
        }

        visited.add(id);

        Person person = personRepository.findByLocalId(id).orElse(null);
        if (person == null) {
            return null;
        }

        PersonDTO dto = personMapper.toDTO(person);

        // Only traverse children
        if (person.getChildren() != null) {
            dto.setChildren(person.getChildren().stream()
                    .map(childRel -> {
                        PersonDTO childDTO = traverseDownwards(
                                childRel.getEntity().getLocalId(),
                                depth - 1,
                                visited
                        );
                        return personMapper.relationshipToDTO(childRel, childDTO);
                    })
                    .toList());
        }

        return dto;
    }

    /**
     * Traverses the full family tree in both directions recursively
     */
    private PersonDTO buildFullTree(String id, int depth, Set<String> visited) {
        Person person = personRepository.findByLocalId(id).orElse(null);
        if (person == null) {
            return null;
        }
        person = combinePerson(person);
        // If depth is 0 or already visited, return person without relationships
        if (depth == 0 || visited.contains(id)) {
            return personMapper.toDTO(person);
        }

        visited.add(id);

        PersonDTO dto = personMapper.toDTO(person);

        // Traverse both parents and children recursively
        if (person.getParents() != null) {
            dto.setParents(person.getParents().stream()
                    .map(parentRel -> {
                        PersonDTO parentDTO = buildFullTree(
                                parentRel.getEntity().getLocalId(),
                                depth - 1,
                                visited
                        );
                        return personMapper.relationshipToDTO(parentRel, parentDTO);
                    })
                    .toList());
        }

        if (person.getChildren() != null) {
            dto.setChildren(person.getChildren().stream()
                    .map(childRel -> {
                        PersonDTO childDTO = buildFullTree(
                                childRel.getEntity().getLocalId(),
                                depth - 1,
                                visited
                        );
                        return personMapper.relationshipToDTO(childRel, childDTO);
                    })
                    .toList());
        }
        // Traverse spouses recursively
        if (person.getSpouses() != null) {
            dto.setSpouses(person.getSpouses().stream()
                    .map(marriedTo -> {
                        PersonDTO spouseDTO = buildFullTree(
                                marriedTo.getSpouse().getLocalId(),
                                depth - 1,
                                visited
                        );
                        return personMapper.marriageToDTO(marriedTo, spouseDTO);
                    })
                    .toList());
        }

        return dto;
    }
}
