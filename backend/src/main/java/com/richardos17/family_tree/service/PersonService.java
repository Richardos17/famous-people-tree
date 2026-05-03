package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonDTO buildPersonTree(String id, int depth, Set<String> visited) {
        if (depth == 0 || visited.contains(id)) {
            return null;
        }

        visited.add(id);

        Person person = personRepository.findByLocalId(id).orElse(null);
        if (person == null) {
            return null;
        }

        PersonDTO dto = personMapper.toDTO(person);

        if (person.getParents() != null) {
            dto.setParents(person.getParents().stream()
                    .map(parentRel -> {
                        PersonDTO parentDTO = buildPersonTree(
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
                        PersonDTO childDTO = buildPersonTree(
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
}
