package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.HashSet;

@RequiredArgsConstructor
@Service
public class PersonQueryService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    /**
     * Load a basic PersonDTO (only person data, no relationships)
     */
    private PersonDTO loadBasicPersonDTO(String id) {
        return personRepository.findByLocalId(id)
                .map(personMapper::toDTO)
                .orElse(null);
    }
    /**
     * Builds a simple tree recursively but in one direction per branch:
     * - Parents traverse only upwards (parents of parents, etc.)
     * - Children traverse only downwards (children of children, etc.)
     */
    public PersonDTO buildSimplePersonTree(String id, int depth) {
        PersonDTO dto = loadBasicPersonDTO(id);
        if (dto == null) {
            return null;
        }

        // Traverse parents upwards only
        var parents = personRepository.findParentsByChildId(id);
        if (!parents.isEmpty()) {
            dto.setParents(parents.stream()
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
        var children = personRepository.findChildrenByParentId(id);
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
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

        PersonDTO dto = loadBasicPersonDTO(id);
        if (dto == null) {
            return null;
        }

        // Only traverse parents
        var parents = personRepository.findParentsByChildId(id);
        if (!parents.isEmpty()) {
            dto.setParents(parents.stream()
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

        PersonDTO dto = loadBasicPersonDTO(id);
        if (dto == null) {
            return null;
        }

        // Only traverse children
        var children = personRepository.findChildrenByParentId(id);
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
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
        PersonDTO dto = loadBasicPersonDTO(id);
        if (dto == null) {
            return null;
        }
        // If depth is 0 or already visited, return person without relationships
        if (depth == 0 || visited.contains(id)) {
            return dto;
        }

        visited.add(id);

        // Traverse parents recursively
        var parents = personRepository.findParentsByChildId(id);
        if (!parents.isEmpty()) {
            dto.setParents(parents.stream()
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

        // Traverse children recursively
        var children = personRepository.findChildrenByParentId(id);
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
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
        var spouses = personRepository.findByLocalId(id).map(Person::getSpouses).orElse(null);
        if (spouses != null && !spouses.isEmpty()) {
            dto.setSpouses(spouses.stream()
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
