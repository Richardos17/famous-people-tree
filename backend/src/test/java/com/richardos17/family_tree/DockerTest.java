package com.richardos17.family_tree;

import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.PersonRepository;
import com.richardos17.family_tree.service.PersonSaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers

public class DockerTest {

    // Define the Neo4j container (specify your target Neo4j image version)
    @Container
    private static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5")
            .withRandomPassword(); // Sets up a secure random password automatically

    // Bind the container properties to Spring Boot properties dynamically
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }
    @Autowired
    PersonSaveService personSaveService;
    @Autowired
    PersonRepository personRepository;
    @Test
    void shouldSaveAndRetrieveMovie() {
        // Given
        Person person = Person.builder()
                .wikidataId("Q937")
                .name("Albert Einstein")
                .birthdate(LocalDate.of(1879, 3, 14))
                .deathdate(LocalDate.of(1955, 4, 18))
                .imageLink("https://example.com/einstein.jpg")
                .wikipediaLink("https://en.wikipedia.org/wiki/Albert_Einstein")
                .height(175)
                .build();

        personSaveService.savePerson(person);

        Person saved = personRepository.findByWikidataId("Q937").orElseThrow();
        assertThat(saved.getName()).isEqualTo("Albert Einstein");
        assertThat(saved.getBirthdate()).isEqualTo(LocalDate.of(1879, 3, 14));
        assertThat(saved.getDeathdate()).isEqualTo(LocalDate.of(1955, 4, 18));
        assertThat(saved.getImageLink()).isEqualTo("https://example.com/einstein.jpg");
        assertThat(saved.getWikipediaLink()).isEqualTo("https://en.wikipedia.org/wiki/Albert_Einstein");
        assertThat(saved.getHeight()).isEqualTo(175);
        assertThat(saved.getRelationshipsExpanded()).isFalse();
        assertThat(saved.getLocalId()).isNotNull();
    }
}