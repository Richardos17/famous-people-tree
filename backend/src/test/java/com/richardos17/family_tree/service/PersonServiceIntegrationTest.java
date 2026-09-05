package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.*;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.CountryRepository;
import com.richardos17.family_tree.repository.PersonRelationshipRepository;
import com.richardos17.family_tree.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests PersonService end-to-end: FetchPerson's JSON parsing runs for real
 * (only the HTTP transport is mocked via WebClient), and the results are saved
 * to a real Neo4j instance via Testcontainers.
 *
 * Compare with PersonSaveIntegrationTest, which mocks FetchPerson entirely and
 * focuses on what PersonSaveService writes to the DB.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class PersonServiceIntegrationTest {

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5")
            .withoutAuthentication();

    @DynamicPropertySource
    static void configureNeo4j(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "");
    }

    // Replaces the WebClient bean in the context — FetchPerson still runs its own parsing logic.
    @MockitoBean
    WebClient wikidataWebClient;

    @SuppressWarnings("rawtypes")
    private final WebClient.RequestHeadersUriSpec requestHeadersUriSpec =
            Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    @SuppressWarnings("rawtypes")
    private final WebClient.RequestHeadersSpec requestHeadersSpec =
            Mockito.mock(WebClient.RequestHeadersSpec.class);
    private final WebClient.ResponseSpec responseSpec =
            Mockito.mock(WebClient.ResponseSpec.class);

    @Autowired
    PersonService personService;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    PersonRelationshipRepository personRelationshipRepository;

    @Autowired
    PersonMapper personMapper;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        countryRepository.deleteAll();

        when(wikidataWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri((URI) any()))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    // ── getPersonsByName ──────────────────────────────────────────────────────

    @Test
    void getPersonsByName_notInDb_parsesWikidataJsonAndSavesPersonWithCountryToDb() {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("""
                {
                  "results": {
                    "bindings": [
                      {
                        "person":            {"value": "http://www.wikidata.org/entity/Q937"},
                        "personLabel":       {"value": "Albert Einstein"},
                        "birthDate":         {"value": "1879-03-14T00:00:00Z"},
                        "deathDate":         {"value": "1955-04-18T00:00:00Z"},
                        "birthCountry":      {"value": "http://www.wikidata.org/entity/Q183"},
                        "birthCountryLabel": {"value": "Germany"}
                      }
                    ]
                  }
                }"""));

        List<Person> result = personService.getPersonsByName("Einstein");

        // Return value is correct
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getWikidataId()).isEqualTo("Q937");
        assertThat(result.getFirst().getName()).isEqualTo("Albert Einstein");

        // Person was actually persisted in Neo4j
        Optional<Person> saved = personRepository.findByWikidataId("Q937");
        assertThat(saved).isPresent();
        assertThat(saved.get().getBirthdate()).isEqualTo(LocalDate.of(1879, 3, 14));
        assertThat(saved.get().getDeathdate()).isEqualTo(LocalDate.of(1955, 4, 18));

        // Country node was created and linked
        assertThat(saved.get().getBornIn()).isNotNull();
        assertThat(saved.get().getBornIn().getWikidataId()).isEqualTo("Q183");
        assertThat(saved.get().getBornIn().getName()).isEqualTo("Germany");
    }

    @Test
    void getPersonsByName_inDb() {
        personRepository.save(Person.builder().wikidataId("Q937").name("Albert Einstein").build());
        List<Person> result = personService.getPersonsByName("Einstein");

        // Return value is correct
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getWikidataId()).isEqualTo("Q937");
        assertThat(result.getFirst().getName()).isEqualTo("Albert Einstein");

        // Person was actually persisted in Neo4j
        Optional<Person> saved = personRepository.findByWikidataId("Q937");
        assertThat(saved).isPresent();
    }

    // ── getPersonByWikidataId ─────────────────────────────────────────────────

    @Test
    void getPersonByWikidataId_notInDb_parsesWikidataJsonAndSavesToDb() {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("""
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q937"},
                        "personLabel": {"value": "Albert Einstein"},
                        "birthDate":   {"value": "1879-03-14T00:00:00Z"},
                        "image":       {"value": "https://example.com/einstein.jpg"},
                        "wikipedia":   {"value": "https://en.wikipedia.org/wiki/Albert_Einstein"}
                      }
                    ]
                  }
                }"""));

        Optional<Person> result = personService.getPersonByWikidataId("Q937");

        assertThat(result).isPresent();
        assertThat(result.get().getImageLink()).isEqualTo("https://example.com/einstein.jpg");
        assertThat(result.get().getWikipediaLink())
                .isEqualTo("https://en.wikipedia.org/wiki/Albert_Einstein");
        assertThat(result.get().getWikidataId()).isEqualTo("Q937");
        assertThat(result.get().getName()).isEqualTo("Albert Einstein");
        assertThat(result.get().getRelationshipsExpanded()).isFalse();
        assertThat(personRepository.findByWikidataId("Q937")).isPresent();
    }
    @Test
    void getPersonByWikidataId_inDb() {
        personRepository.save(Person.builder().wikidataId("Q937").name("Albert Einstein").build());

        Optional<Person> result = personService.getPersonByWikidataId("Q937");

        assertThat(result).isPresent();
        assertThat(result.get().getWikidataId()).isEqualTo("Q937");
        assertThat(result.get().getName()).isEqualTo("Albert Einstein");
    }

    // get person tree
    @Test
    void getPersonTreeJustSpousesByWikidataId_notInDb_parsesWikidataJsonAndSavesToDb() {
        String personReturn = """
                {
                                  "results": {
                                    "bindings": [
                                      {
                                        "person":      {"value": "http://www.wikidata.org/entity/Q937"},
                                        "personLabel": {"value": "Albert Einstein"}
                                      }
                                    ]
                                  }
                                }""";
        String spousesReturn = """
                {
                  "results": {
                    "bindings": [
                      {
                         "spouses": {
                                  "value": "{\\"id\\":\\"Q60197\\", \\"start\\":\\"2000-01-05T00:00:00Z\\",\\"end\\":\\"2005-04-07T00:00:00Z\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String spousePersonReturn = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q60197"},
                        "personLabel": {"value": "Hans Albert Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(personReturn))
                .thenReturn(Mono.just(spousesReturn))
                .thenReturn(Mono.just(spousePersonReturn));


        Optional<PersonTreeResponseDTO> result = personService.getPersonTreeByWikidataId("Q937", 2, false);

        assertThat(result).isPresent();
        assertThat(result.get().getPersons())
                .containsExactlyInAnyOrder(
                        PersonDTO.builder().name("Albert Einstein").wikidataId("Q937").build(),
                        PersonDTO.builder().name("Hans Albert Einstein").wikidataId("Q60197").build());
        assertThat(result.get().getRelationships())
                .containsExactlyInAnyOrder(
                        new MarriageDTO("Q937", "Q60197",
                                LocalDate.of(2000, 1, 5), LocalDate.of(2005, 4, 7)));


        assertThat(personRepository.findByWikidataId("Q937")).isPresent();
        List<MarriedTo> saved = personRelationshipRepository.findSpousesByPersonId("Q937");
        assertThat(saved).hasSize(1);
        MarriedTo marriage = saved.getFirst();
        assertThat(marriage.getStartDate()).isEqualTo(LocalDate.of(2000, 1, 5));
        assertThat(marriage.getEndDate()).isEqualTo(LocalDate.of(2005, 4, 7));
        assertThat(marriage.getSpouse().getName()).isEqualTo("Hans Albert Einstein");
        assertThat(marriage.getSpouse().getWikidataId()).isEqualTo("Q60197");
    }
    @Test
    void getPersonTreeJustSpousesByWikidataId_inDb_parsesWikidataJsonAndSavesToDb() {
        personRepository.save(Person.builder().wikidataId("Q937").name("Albert Einstein").build());
        String spousesReturn = """
                {
                  "results": {
                    "bindings": [
                      {
                         "spouses": {
                                  "value": "{\\"id\\":\\"Q60197\\", \\"start\\":\\"2000-01-05T00:00:00Z\\",\\"end\\":\\"2005-04-07T00:00:00Z\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String spousePersonReturn = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q60197"},
                        "personLabel": {"value": "Hans Albert Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(spousesReturn))
                .thenReturn(Mono.just(spousePersonReturn));


        Optional<PersonTreeResponseDTO> result = personService.getPersonTreeByWikidataId("Q937", 2, false);

        assertThat(result).isPresent();
        assertThat(result.get().getPersons())
                .containsExactlyInAnyOrder(
                        PersonDTO.builder().name("Albert Einstein").wikidataId("Q937").build(),
                        PersonDTO.builder().name("Hans Albert Einstein").wikidataId("Q60197").build());
        assertThat(result.get().getRelationships())
                .containsExactlyInAnyOrder(
                        new MarriageDTO("Q937", "Q60197",
                                LocalDate.of(2000, 1, 5), LocalDate.of(2005, 4, 7)));


        assertThat(personRepository.findByWikidataId("Q937")).isPresent();
        List<MarriedTo> saved = personRelationshipRepository.findSpousesByPersonId("Q937");
        assertThat(saved).hasSize(1);
        MarriedTo marriage = saved.getFirst();
        assertThat(marriage.getStartDate()).isEqualTo(LocalDate.of(2000, 1, 5));
        assertThat(marriage.getEndDate()).isEqualTo(LocalDate.of(2005, 4, 7));
        assertThat(marriage.getSpouse().getName()).isEqualTo("Hans Albert Einstein");
        assertThat(marriage.getSpouse().getWikidataId()).isEqualTo("Q60197");
    }
    @Test
    void getPersonTreeJustSpousesByWikidataId_allInDb_parsesWikidataJsonAndSavesToDb() {
        Person person1 = personRepository.save(Person.builder().wikidataId("Q937").name("Albert Einstein").relationshipsExpanded(true).build());
        Person person2 = personRepository.save(Person.builder().wikidataId("Q60197").name("Hans Albert Einstein").build());
        personRepository.createMarriageRelationship(person1.getLocalId(), person2.getLocalId(), LocalDate.of(2000, 1, 5), LocalDate.of(2005, 4, 7));

        Optional<PersonTreeResponseDTO> result = personService.getPersonTreeByWikidataId("Q937", 2, false);

        assertThat(result).isPresent();
        assertThat(result.get().getPersons())
                .containsExactlyInAnyOrder(
                        personMapper.toDTO(person1),
                        personMapper.toDTO(person2));
        assertThat(result.get().getRelationships())
                .containsExactlyInAnyOrder(
                        new MarriageDTO("Q937", "Q60197",
                                LocalDate.of(2000, 1, 5), LocalDate.of(2005, 4, 7)));
    }
    @Test
    void getPersonTreeByWikidataId_notInDb_parsesWikidataJsonAndSavesToDb() {
        String personReturn = """
                {
                                  "results": {
                                    "bindings": [
                                      {
                                        "person":      {"value": "http://www.wikidata.org/entity/Q937"},
                                        "personLabel": {"value": "Albert Einstein"}
                                      }
                                    ]
                                  }
                                }""";
        String spouseResponse = """
                {
                  "results": {
                    "bindings": [
                      {
                         "spouses": {
                                  "value": ""
                                }
                      }
                    ]
                  }
                }""";
        String parentReturn = """
                {
                  "results": {
                    "bindings": [
                      {
                         "parents": {
                                  "value": "{\\"id\\":\\"Q6014\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String childrenReturn = """
                {
                  "results": {
                    "bindings": [
                      {
                         "children": {
                                  "value": "{\\"id\\":\\"Q6017\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String persons2Return = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q6014"},
                        "personLabel": {"value": "Hans Albert Einstein"}
                      }
                    ]
                  }
                }""";
        String persons3Return = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q6017"},
                        "personLabel": {"value": "Martin Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(personReturn))
                .thenReturn(Mono.just(spouseResponse))
                .thenReturn(Mono.just(parentReturn))
                .thenReturn(Mono.just(persons2Return))
                .thenReturn(Mono.just(childrenReturn))
                .thenReturn(Mono.just(persons3Return));


        Optional<PersonTreeResponseDTO> result = personService.getPersonTreeByWikidataId("Q937", 1, false);

        assertThat(result).isPresent();
        assertThat(result.get().getPersons())
                .containsExactlyInAnyOrder(
                        PersonDTO.builder().name("Albert Einstein").wikidataId("Q937").build(),
                        PersonDTO.builder().name("Hans Albert Einstein").wikidataId("Q6014").build(),
                        PersonDTO.builder().name("Martin Einstein").wikidataId("Q6017").build());
        assertThat(result.get().getRelationships())
                .containsExactlyInAnyOrder(
                        new ParentRelationshipDTO("Q937", "Q6014"),
                        new ParentRelationshipDTO("Q6017", "Q937"));


        assertThat(personRepository.findByWikidataId("Q937")).isPresent();
        List<FamilyRelationship> savedChildren = personRelationshipRepository.findChildrenByParentId("Q937");
        List<FamilyRelationship> savedParents = personRelationshipRepository.findParentsByChildId("Q937");
        assertThat(savedChildren).hasSize(1);
        assertThat(savedParents).hasSize(1);
        FamilyRelationship childRelationship = savedChildren.getFirst();
        FamilyRelationship parentRelationship = savedParents.getFirst();
        assertThat(childRelationship.getEntity().getName()).isEqualTo("Martin Einstein");
        assertThat(childRelationship.getEntity().getWikidataId()).isEqualTo("Q6017");
        assertThat(parentRelationship.getEntity().getName()).isEqualTo("Hans Albert Einstein");
        assertThat(parentRelationship.getEntity().getWikidataId()).isEqualTo("Q6014");
    }
    @Test
    void getPersonTreeByWikidataId_inDb() {
        Person parent = personRepository.save(Person.builder().wikidataId("Q937").name("Albert Einstein").relationshipsExpanded(true).build());
        Person child = personRepository.save(Person.builder().wikidataId("Q6017").name("Martin Einstein").build());
        Person grandParent = personRepository.save(Person.builder().wikidataId("Q6014").name("Hans Albert Einstein").build());
        personRepository.createParentRelationship(parent.getLocalId(), child.getLocalId());
        personRepository.createParentRelationship(grandParent.getLocalId(), parent.getLocalId());

        Optional<PersonTreeResponseDTO> result = personService.getPersonTreeByWikidataId("Q937", 1, false);

        assertThat(result).isPresent();
        assertThat(result.get().getPersons())
                .containsExactlyInAnyOrder(
                        PersonDTO.builder().name("Albert Einstein").wikidataId("Q937").build(),
                        PersonDTO.builder().name("Hans Albert Einstein").wikidataId("Q6014").build(),
                        PersonDTO.builder().name("Martin Einstein").wikidataId("Q6017").build());
        assertThat(result.get().getRelationships())
                .containsExactlyInAnyOrder(
                        new ParentRelationshipDTO("Q937", "Q6014"),
                        new ParentRelationshipDTO("Q6017", "Q937"));
    }
    @Test
    void getPersonTreeByWikidataId_inDb_moreGenerationsParents() {
        Person child = personRepository.save(Person.builder().wikidataId("Q937").name("Albert Einstein").relationshipsExpanded(true).build());
        Person parent = personRepository.save(Person.builder().wikidataId("Q6017").name("Martin Einstein").relationshipsExpanded(true).build());
        Person grandParent = personRepository.save(Person.builder().wikidataId("Q6014").name("Hans Albert Einstein").build());
        personRepository.createParentRelationship(parent.getLocalId(), child.getLocalId());
        personRepository.createParentRelationship(grandParent.getLocalId(), parent.getLocalId());

        Optional<PersonTreeResponseDTO> result = personService.getPersonTreeByWikidataId("Q937", 2, false);

        assertThat(result).isPresent();
        assertThat(result.get().getPersons())
                .containsExactlyInAnyOrder(
                        PersonDTO.builder().name("Albert Einstein").wikidataId("Q937").build(),
                        PersonDTO.builder().name("Hans Albert Einstein").wikidataId("Q6014").build(),
                        PersonDTO.builder().name("Martin Einstein").wikidataId("Q6017").build());
        assertThat(result.get().getRelationships())
                .containsExactlyInAnyOrder(
                        new ParentRelationshipDTO("Q937", "Q6017"),
                        new ParentRelationshipDTO("Q6017", "Q6014"));

    }
    @Test
    void getPersonTreeByWikidataId_notInDb_moreGenerationsChildren_parsesWikidataJsonAndSavesToDb() {
        String personReturn = """
                {
                                  "results": {
                                    "bindings": [
                                      {
                                        "person":      {"value": "http://www.wikidata.org/entity/Q937"},
                                        "personLabel": {"value": "Albert Einstein"}
                                      }
                                    ]
                                  }
                                }""";
        String spouseResponse = """
                {
                  "results": {
                    "bindings": [
                      {
                         "spouses": {
                                  "value": ""
                                }
                      }
                    ]
                  }
                }""";
        String parentReturn = """
                {
                  "results": {
                    "bindings": [
                      {
                         "parents": {
                                  "value": ""
                                }
                      }
                    ]
                  }
                }""";
        String children1Return = """
                {
                  "results": {
                    "bindings": [
                      {
                         "children": {
                                  "value": "{\\"id\\":\\"Q6017\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String persons1Return = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q6017"},
                        "personLabel": {"value": "Martin Einstein"}
                      }
                    ]
                  }
                }""";
        String children2Return = """
                {
                  "results": {
                    "bindings": [
                      {
                         "children": {
                                  "value": "{\\"id\\":\\"Q6014\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String persons2Return = """
                {
                  "results": {
                    "bindings": [
                     {
                        "person":      {"value": "http://www.wikidata.org/entity/Q6014"},
                        "personLabel": {"value": "Hans Albert Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(personReturn))
                .thenReturn(Mono.just(spouseResponse))
                .thenReturn(Mono.just(parentReturn))
                .thenReturn(Mono.just(children1Return))
                .thenReturn(Mono.just(persons1Return))
                .thenReturn(Mono.just(children2Return))
                .thenReturn(Mono.just(persons2Return));


        Optional<PersonTreeResponseDTO> result = personService.getPersonTreeByWikidataId("Q937", 2, false);

        assertThat(result).isPresent();
        assertThat(result.get().getPersons())
                .containsExactlyInAnyOrder(
                        PersonDTO.builder().name("Albert Einstein").wikidataId("Q937").build(),
                        PersonDTO.builder().name("Hans Albert Einstein").wikidataId("Q6014").build(),
                        PersonDTO.builder().name("Martin Einstein").wikidataId("Q6017").build());
        assertThat(result.get().getRelationships())
                .containsExactlyInAnyOrder(
                        new ParentRelationshipDTO("Q6014", "Q6017"),
                        new ParentRelationshipDTO("Q6017", "Q937"));


        assertThat(personRepository.findByWikidataId("Q937")).isPresent();
        List<FamilyRelationship> savedChildren = personRelationshipRepository.findChildrenByParentId("Q937");
        List<FamilyRelationship> savedGrandChildren = personRelationshipRepository.findChildrenByParentId("Q6017");
        assertThat(savedChildren).hasSize(1);
        assertThat(savedGrandChildren).hasSize(1);
        FamilyRelationship childRelationship = savedChildren.getFirst();
        FamilyRelationship grandChildRelationship = savedGrandChildren.getFirst();
        assertThat(childRelationship.getEntity().getName()).isEqualTo("Martin Einstein");
        assertThat(childRelationship.getEntity().getWikidataId()).isEqualTo("Q6017");
        assertThat(grandChildRelationship.getEntity().getName()).isEqualTo("Hans Albert Einstein");
        assertThat(grandChildRelationship.getEntity().getWikidataId()).isEqualTo("Q6014");
    }
}