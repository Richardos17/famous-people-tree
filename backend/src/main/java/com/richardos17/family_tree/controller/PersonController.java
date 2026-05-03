package com.richardos17.family_tree.controller;

import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {
    private final PersonRepository personRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable String id) {
        Optional<Person> optionalPerson = personRepository.findByLocalId(id);

        if (optionalPerson.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Person person = optionalPerson.get();

        List<FamilyRelationship<Person>> children = personRepository.findChildrenByParentId(id);
        person.setChildren(children);

        List<FamilyRelationship<Person>> parents = personRepository.findParentsByChildId(id);
        person.setParents(parents);

        return ResponseEntity.ok(person);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<Person>> getPersonByName(@PathVariable String name) {
        List<Person> people = personRepository.findByName(name);
        if (people.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(people);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Person>> getPersonSearch(@RequestParam String name) {
        List<Person> people = personRepository.findByNameContainingIgnoreCase(name);
        if (people.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(people);
    }
    
    @GetMapping("/{id}/parents")
    public ResponseEntity<List<FamilyRelationship<Person>>> getParents(@PathVariable String id) {
        List<FamilyRelationship<Person>> people = personRepository.findParentsByChildId(id);
        if (people.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(people);
    }
    
    @GetMapping("/{id}/children")
    public ResponseEntity<List<FamilyRelationship<Person>>> getChildren(@PathVariable String id) {
        List<FamilyRelationship<Person>> people = personRepository.findChildrenByParentId(id);
        if (people.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(people);
    }
}
