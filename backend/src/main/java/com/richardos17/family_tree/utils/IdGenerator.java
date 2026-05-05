package com.richardos17.family_tree.utils;

import com.richardos17.family_tree.repository.CountryRepository;
import com.richardos17.family_tree.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdGenerator {

    private final PersonRepository personRepository;
    private final CountryRepository countryRepository;
    /**
     * Generates a unique ID using UUID v4
     */
    public String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a unique ID and verifies it doesn't already exist in the database
     */
    public String generateUniquePersonId() {
        String id;
        do {
            id = generateId();
        } while (personRepository.existsByLocalId(id));
        return id;
    }
    public String generateUniqueCountryId() {
        String id;
        do {
            id = generateId();
        } while (countryRepository.existsByLocalId(id));
        return id;
    }
}
