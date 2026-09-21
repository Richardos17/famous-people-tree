package com.richardos17.family_tree.controller;

import com.richardos17.family_tree.DTOs.PersonDTO;
import com.richardos17.family_tree.DTOs.PersonTreeResponseDTO;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import com.richardos17.family_tree.service.PersonService;
import com.richardos17.family_tree.service.PersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/person")
public class PersonController {

    private static final String DEFAULT_TREE_DEPTH = "1";

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PersonService personService;

    @GetMapping("/{wikidataId}")
    public ResponseEntity<PersonDTO> getPersonByWikidataId(@PathVariable String wikidataId) {
        return personService.getPersonByWikidataId(wikidataId)
                .map(personMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/name/{name}")
    public ResponseEntity<List<PersonDTO>> getPersonByName(@PathVariable String name) {
        List<PersonDTO> people = personService.getPersonsByName(name).stream()
                .map(personMapper::toDTO)
                .toList();
        return people.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(people);
    }

    @GetMapping("/{wikidataId}/full_tree")
    public ResponseEntity<PersonTreeResponseDTO> getPersonFullTree(@PathVariable String wikidataId, @RequestParam(defaultValue = DEFAULT_TREE_DEPTH) int depth) {
        Optional<PersonTreeResponseDTO> person = personService.getPersonTreeByWikidataId(wikidataId, depth, true);
        if (person.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        System.out.println(person.get());
        return ResponseEntity.ok(person.get());
    }

    @GetMapping("/{wikidataId}/direct_tree")
    public ResponseEntity<PersonTreeResponseDTO> getPersonDirectTree(@PathVariable String wikidataId, @RequestParam(defaultValue = DEFAULT_TREE_DEPTH) int depth) {
        Optional<PersonTreeResponseDTO> person = personService.getPersonTreeByWikidataId(wikidataId, depth, false);
        if (person.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        System.out.println(person.get());
        return ResponseEntity.ok(person.get());
    }
}
