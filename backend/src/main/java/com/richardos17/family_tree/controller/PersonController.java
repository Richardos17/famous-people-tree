package com.richardos17.family_tree.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/person")
public class PersonController {
    private final PersonRepository personRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable String id) {
        return personRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
}
