package com.richardos17.family_tree.service;

import com.richardos17.family_tree.domain.ExpandedPerson;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FetchPersonTest {

    @Mock
    private WebClient wikidataWebClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private FetchPerson fetchPerson;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        when(wikidataWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri((URI) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    // ── fetchPersonsByName ────────────────────────────────────────────────────

    @Test
    void fetchPersonsByName_nullName_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> fetchPerson.fetchPersonsByName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void fetchPersonsByName_blankName_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> fetchPerson.fetchPersonsByName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fetchPersonsByName_validName_returnsMappedPersons() {
        String response = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":           {"value": "http://www.wikidata.org/entity/Q937"},
                        "personLabel":      {"value": "Albert Einstein"},
                        "birthDate":        {"value": "1879-03-14T00:00:00Z"},
                        "deathDate":        {"value": "1955-04-18T00:00:00Z"},
                        "birthCountry":     {"value": "http://www.wikidata.org/entity/Q183"},
                        "birthCountryLabel":{"value": "Germany"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(response));

        List<Person> result = fetchPerson.fetchPersonsByName("Einstein");

        assertThat(result).hasSize(1);
        Person person = result.get(0);
        assertThat(person.getWikidataId()).isEqualTo("Q937");
        assertThat(person.getName()).isEqualTo("Albert Einstein");
        assertThat(person.getBirthdate()).isEqualTo(LocalDate.of(1879, 3, 14));
        assertThat(person.getDeathdate()).isEqualTo(LocalDate.of(1955, 4, 18));
        assertThat(person.getBornIn()).isNotNull();
        assertThat(person.getBornIn().getName()).isEqualTo("Germany");
        assertThat(person.getBornIn().getWikidataId()).isEqualTo("Q183");
    }

    @Test
    void fetchPersonsByName_emptyBindings_returnsEmptyList() {
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("{\"results\":{\"bindings\":[]}}"));

        assertThat(fetchPerson.fetchPersonsByName("Unknown")).isEmpty();
    }

    // ── fetchPersonByWikidataId ───────────────────────────────────────────────

    @Test
    void fetchPersonByWikidataId_nullId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> fetchPerson.fetchPersonByWikidataId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fetchPersonByWikidataId_emptyId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> fetchPerson.fetchPersonByWikidataId(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fetchPersonByWikidataId_noResultsFromWikidata_throwsIllegalArgumentException() {
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("{\"results\":{\"bindings\":[]}}"));

        assertThatThrownBy(() -> fetchPerson.fetchPersonByWikidataId("Q99999999"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("No person found for Wikidata ID: Q99999999");
    }

    @Test
    void fetchPersonByWikidataId_validId_returnsPerson() {
        String response = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q937"},
                        "personLabel": {"value": "Albert Einstein"},
                        "birthDate":   {"value": "1879-03-14T00:00:00Z"},
                        "image":       {"value": "http://example.com/einstein.jpg"},
                        "wikipedia":   {"value": "https://en.wikipedia.org/wiki/Albert_Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(response));

        Person result = fetchPerson.fetchPersonByWikidataId("Q937");

        assertThat(result.getWikidataId()).isEqualTo("Q937");
        assertThat(result.getName()).isEqualTo("Albert Einstein");
        assertThat(result.getBirthdate()).isEqualTo(LocalDate.of(1879, 3, 14));
        assertThat(result.getImageLink()).isEqualTo("http://example.com/einstein.jpg");
        assertThat(result.getWikipediaLink()).isEqualTo("https://en.wikipedia.org/wiki/Albert_Einstein");
        assertThat(result.getBornIn()).isNull();
    }

    // ── fetchFullPersonByWikidataId ───────────────────────────────────────────

    @Test
    void fetchChildrenByWikidataId() {
        String firstResponse = """
                {
                  "results": {
                    "bindings": [
                      {
                         "children": {
                                  "value": "{\\"id\\":\\"Q60197\\"},{\\"id\\":\\"Q7197\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String secondReponse = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q60197"},
                        "personLabel": {"value": "Hans Albert Einstein"}
                      },
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q7197"},
                        "personLabel": {"value": "Martin Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(firstResponse))
                .thenReturn(Mono.just(secondReponse));

        List<FamilyRelationship> result = fetchPerson.fetchChildrenByWikidataId("Q937");

        assertThat(result).containsExactlyInAnyOrder(
                new FamilyRelationship(null, Person.builder().wikidataId("Q60197").name("Hans Albert Einstein").build()),
                new FamilyRelationship(null, Person.builder().wikidataId("Q7197").name("Martin Einstein").build()));
    }
    @Test
    void fetchParentsByWikidataId() {
        String firstResponse = """
                {
                  "results": {
                    "bindings": [
                      {
                         "parents": {
                                  "value": "{\\"id\\":\\"Q60197\\"},{\\"id\\":\\"Q7197\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String secondReponse = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q60197"},
                        "personLabel": {"value": "Hans Albert Einstein"}
                      },
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q7197"},
                        "personLabel": {"value": "Martin Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(firstResponse))
                .thenReturn(Mono.just(secondReponse));

        List<FamilyRelationship> result = fetchPerson.fetchParentsByWikidataId("Q937");

        assertThat(result).containsExactlyInAnyOrder(
                new FamilyRelationship(null, Person.builder().wikidataId("Q60197").name("Hans Albert Einstein").build()),
                new FamilyRelationship(null, Person.builder().wikidataId("Q7197").name("Martin Einstein").build()));
    }
    @Test
    void fetchSpousesByWikidataId() {
        String firstResponse = """
                {
                  "results": {
                    "bindings": [
                      {
                         "spouses": {
                                  "value": "{\\"id\\":\\"Q60197\\"},{\\"id\\":\\"Q7197\\", \\"start\\":\\"2000-01-05T00:00:00Z\\",\\"end\\":\\"2005-04-07T00:00:00Z\\"}"
                                }
                      }
                    ]
                  }
                }""";
        String secondReponse = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q60197"},
                        "personLabel": {"value": "Hans Albert Einstein"}
                      },
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q7197"},
                        "personLabel": {"value": "Martin Einstein"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(firstResponse))
                .thenReturn(Mono.just(secondReponse));

        List<MarriedTo> result = fetchPerson.fetchSpousesByWikidataId("Q937");

        assertThat(result).containsExactlyInAnyOrder(
                new MarriedTo(null, Person.builder().wikidataId("Q60197").name("Hans Albert Einstein").build(), null, null),
                new MarriedTo(null, Person.builder().wikidataId("Q7197").name("Martin Einstein").build(),
                LocalDate.of(2000, 1, 5), LocalDate.of(2005, 4, 7)));
    }
}
