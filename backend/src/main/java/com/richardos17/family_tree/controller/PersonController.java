package com.richardos17.family_tree.controller;

import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.RelationshipDTO;
import com.richardos17.family_tree.repository.PersonRepository;
import com.richardos17.family_tree.service.PersonMapper;
import com.richardos17.family_tree.service.PersonQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {

    private static final int DEFAULT_TREE_DEPTH = 2;

    private final PersonRepository personRepository;
    private final PersonQueryService personQueryService;
    private final PersonMapper personMapper;

    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable String id) {
        return personRepository.findByLocalId(id)
                .map(person -> personMapper.toDTO(person))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<PersonDTO>> getPersonByName(@PathVariable String name) {
        List<PersonDTO> people = personRepository.findByName(name).stream()
                .map(personMapper::toDTO)
                .toList();
        return people.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(people);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PersonDTO>> getPersonSearch(@RequestParam String name) {
        List<PersonDTO> people = personRepository.searchByName(name).stream()
                .map(personMapper::toDTO)
                .toList();
        return people.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(people);
    }

    @GetMapping("/{id}/parents")
    public ResponseEntity<List<RelationshipDTO>> getParents(@PathVariable String id) {
        List<RelationshipDTO> parentDTOs = personRepository.findParentsByChildId(id)
            .stream()
            .map(personMapper::relationshipToDTO)
            .toList();

        return parentDTOs.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(parentDTOs);
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<RelationshipDTO>> getChildren(@PathVariable String id) {
        List<RelationshipDTO> childrenDTOs = personRepository.findChildrenByParentId(id)
            .stream()
            .map(personMapper::relationshipToDTO)
            .toList();

        return childrenDTOs.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(childrenDTOs);
    }

    @GetMapping("/{id}/full_tree")
    public ResponseEntity<PersonDTO> getPersonFullTree(@PathVariable String id) {
        return ResponseEntity.ok(personQueryService.buildPersonTree(id, DEFAULT_TREE_DEPTH));
    }

    @GetMapping("/{id}/simple_tree")
    public ResponseEntity<PersonDTO> getPersonSimpleTree(@PathVariable String id) {
        return ResponseEntity.ok(personQueryService.buildSimplePersonTree(id, DEFAULT_TREE_DEPTH));
    }
}
