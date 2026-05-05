package com.richardos17.family_tree.controller;

import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import com.richardos17.family_tree.service.PersonMapper;
import com.richardos17.family_tree.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {

    private static final int DEFAULT_TREE_DEPTH = 2;

    private final PersonRepository personRepository;
    private final PersonService personService;
    private final PersonMapper personMapper;

    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable String id) {
        Optional<Person> optionalPerson = personRepository.findByLocalId(id);

        if (optionalPerson.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PersonDTO person = personMapper.toDTO(personService.combinePerson(optionalPerson.get()));
        return ResponseEntity.ok(person);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<PersonDTO>> getPersonByName(@PathVariable String name) {
        List<Person> people = personRepository.findByName(name);

        if (people.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(people.stream().map(personService::combinePerson).map(personMapper::toDTO).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PersonDTO>> getPersonSearch(@RequestParam String name) {
        List<Person> people = personRepository.searchByName(name);
        if (people.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(people.stream().map(personService::combinePerson).map(personMapper::toDTO).toList());
    }

    @GetMapping("/{id}/parents")
    public ResponseEntity<List<RelationshipDTO>> getParents(@PathVariable String id) {
        List<RelationshipDTO> parentDTOs = personRepository.findParentsByChildId(id)
            .stream()
            .map(personMapper::relationshipToDTO)
            .toList();

        if (parentDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(parentDTOs);
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<RelationshipDTO>> getChildren(@PathVariable String id) {
        List<RelationshipDTO> childrenDTOs = personRepository.findChildrenByParentId(id)
            .stream()
            .map(personMapper::relationshipToDTO)
            .toList();

        if (childrenDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(childrenDTOs);
    }

    @GetMapping("/{id}/full_tree")
    public ResponseEntity<PersonDTO> getPersonFullTree(@PathVariable String id) {
        return ResponseEntity.ok(personService.buildPersonTree(id, DEFAULT_TREE_DEPTH));
    }

    @GetMapping("/{id}/simple_tree")
    public ResponseEntity<PersonDTO> getPersonSimpleTree(@PathVariable String id) {
        return ResponseEntity.ok(personService.buildSimplePersonTree(id, DEFAULT_TREE_DEPTH));
    }
}
