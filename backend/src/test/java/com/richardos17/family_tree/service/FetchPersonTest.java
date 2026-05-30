package com.richardos17.family_tree.service;

import com.richardos17.family_tree.domain.ExpandedPerson;
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
        when(requestHeadersUriSpec.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())).thenReturn(requestHeadersSpec);
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
                .isInstanceOf(IllegalArgumentException.class)
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
    void fetchFullPersonByWikidataId_nullId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> fetchPerson.fetchFullPersonByWikidataId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fetchFullPersonByWikidataId_noRelatives_returnsExpandedPersonWithRelationshipsExpanded() {
        String response = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q937"},
                        "personLabel": {"value": "Albert Einstein"},
                        "birthDate":   {"value": "1879-03-14T00:00:00Z"}
                      }
                    ]
                  }
                }""";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(response));

        ExpandedPerson result = fetchPerson.fetchFullPersonByWikidataId("Q937");

        assertThat(result.person().getWikidataId()).isEqualTo("Q937");
        assertThat(result.person().getRelationshipsExpanded()).isTrue();
        assertThat(result.childRelationships()).isEmpty();
    }

    @Test
    void fetchFullPersonByWikidataId_withChildren_populatesChildRelationships() {
        String mainResponse = """
                {
                  "results": {
                    "bindings": [
                      {
                        "person":      {"value": "http://www.wikidata.org/entity/Q937"},
                        "personLabel": {"value": "Albert Einstein"},
                        "children":    {"value": "{\\"id\\":\\"Q60197\\"}"}
                      }
                    ]
                  }
                }""";
        String relativesResponse = """
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
                .thenReturn(Mono.just(mainResponse))
                .thenReturn(Mono.just(relativesResponse));

        ExpandedPerson result = fetchPerson.fetchFullPersonByWikidataId("Q937");

        assertThat(result.childRelationships()).hasSize(1);
        assertThat(result.childRelationships().get(0).getEntity().getWikidataId()).isEqualTo("Q60197");
        assertThat(result.childRelationships().get(0).getEntity().getName()).isEqualTo("Hans Albert Einstein");
    }
}
