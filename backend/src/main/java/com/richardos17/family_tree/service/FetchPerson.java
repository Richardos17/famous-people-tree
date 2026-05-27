package com.richardos17.family_tree.service;

import com.richardos17.family_tree.config.WikidataLocalDateDeserializer;
import com.richardos17.family_tree.domain.Country;
import com.richardos17.family_tree.domain.ExpandedPerson;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FetchPerson {

    private final WebClient wikidataWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetches all persons with the given name.
     * @param name The name of the person to search for.
     * @return A list of Person objects representing the persons with the given name.
     */
    public List<Person> fetchPersonsByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        String sparql = String.format("""
                SELECT
                    ?person
                    ?personLabel
                    ?birthDate
                    ?deathDate
                    ?image
                    ?height
                    ?wikipedia
                    ?birthCountry
                    ?birthCountryLabel
                WHERE {
                    SERVICE wikibase:mwapi {
                        bd:serviceParam wikibase:api "EntitySearch" .
                        bd:serviceParam wikibase:endpoint "www.wikidata.org" .
                        bd:serviceParam mwapi:search "%s" .
                        bd:serviceParam mwapi:language "en" .
                        ?person wikibase:apiOutputItem mwapi:item .
                    }
                    ?person wdt:P31 wd:Q5 .
                    OPTIONAL { ?person wdt:P569 ?birthDate }
                    OPTIONAL { ?person wdt:P570 ?deathDate }
                    OPTIONAL { ?person wdt:P18 ?image }
                    OPTIONAL { ?person wdt:P2048 ?height }

                    OPTIONAL {
                        ?person wdt:P19 ?birthPlace .
                        ?birthPlace wdt:P17 ?birthCountry .
                    }

                    OPTIONAL {
                        ?wikipedia schema:about ?person ;
                                   schema:isPartOf <https://en.wikipedia.org/> .
                    }

                    SERVICE wikibase:label {
                        bd:serviceParam wikibase:language "en" .
                    }
                }
                """, name);

        return fetchData(sparql).valueStream()
                .map(this::buildPersonFromProperties)
                .toList();
    }

    /**
     * Fetches the person with the given wikidataId.
     * @param wikidataId The Wikidata ID of the person.
     * @return The Person object representing the person with the given wikidataId.
     */
    public Person fetchPersonByWikidataId(String wikidataId) {
        if (wikidataId == null || wikidataId.isBlank()) {
            throw new IllegalArgumentException("Wikidata ID cannot be null or empty");
        }
        String sparql = String.format("""
                SELECT
                    ?person
                    ?personLabel
                    ?birthDate
                    ?deathDate
                    ?image
                    ?height
                    ?wikipedia
                    ?birthCountry
                    ?birthCountryLabel
                WHERE {
                    BIND(wd:%s AS ?person)
                    ?person wdt:P31 wd:Q5 .
                    OPTIONAL { ?person wdt:P569 ?birthDate }
                    OPTIONAL { ?person wdt:P570 ?deathDate }
                    OPTIONAL { ?person wdt:P18 ?image }
                    OPTIONAL { ?person wdt:P2048 ?height }

                    OPTIONAL {
                        ?person wdt:P19 ?birthPlace .
                        ?birthPlace wdt:P17 ?birthCountry .
                    }

                    OPTIONAL {
                        ?wikipedia schema:about ?person ;
                                   schema:isPartOf <https://en.wikipedia.org/> .
                    }

                    SERVICE wikibase:label {
                        bd:serviceParam wikibase:language "en" .
                    }
                }
                """, wikidataId);

        JsonNode results = fetchData(sparql);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("No person found for Wikidata ID: " + wikidataId);
        }
        return buildPersonFromProperties(results.get(0));
    }

    /**
     * Fetches the full information about a person and its relatives.
     * @param wikidataId The Wikidata ID of exisiting person.
     * @return The ExpandedPerson object containing the full information about the person and its relatives.
     */
    public ExpandedPerson fetchFullPersonByWikidataId(String wikidataId) {
        if (wikidataId == null || wikidataId.isBlank()) {
            throw new IllegalArgumentException("Wikidata ID cannot be null or empty");
        }
        JsonNode row = fetchData(buildFullPersonSparql(wikidataId)).get(0);
        if (row == null) {
            throw new IllegalArgumentException("No data found for Wikidata ID: " + wikidataId);
        }
        Person person = buildPersonFromProperties(row);
        person.setRelationshipsExpanded(true);

        Map<String, List<Relative>> relatives = parseRelatives(row);
        List<Person> relativePersons = fetchRelativePersons(relatives);

        return assembleExpandedPerson(person, relatives, relativePersons);
    }

    private String buildFullPersonSparql(String wikidataId) {
        return String.format("""
                SELECT
                    ?person ?personLabel ?birthDate ?deathDate ?image ?height ?wikipedia ?birthCountry ?birthCountryLabel
                    (GROUP_CONCAT(DISTINCT ?childJson;  separator=",") AS ?children)
                    (GROUP_CONCAT(DISTINCT ?fatherJson; separator=",") AS ?fathers)
                    (GROUP_CONCAT(DISTINCT ?motherJson; separator=",") AS ?mothers)
                    (GROUP_CONCAT(DISTINCT ?spouseJson; separator=",") AS ?spouses)
                WHERE {
                    BIND(wd:%s AS ?person)

                    # ---------------- MAIN PERSON ----------------
                    OPTIONAL { ?person wdt:P569 ?birthDate }
                    OPTIONAL { ?person wdt:P570 ?deathDate }
                    OPTIONAL { ?person wdt:P18  ?image }
                    OPTIONAL { ?person wdt:P2048 ?height }

                    OPTIONAL {
                        ?person wdt:P19 ?birthPlace .
                        ?birthPlace wdt:P17 ?birthCountry .
                    }

                    OPTIONAL {
                        ?article <http://schema.org/about> ?person ;
                                 <http://schema.org/isPartOf> <https://en.wikipedia.org/> .
                    }

                    # ---------------- CHILDREN ----------------
                    OPTIONAL {
                        ?person wdt:P40 ?child .
                        BIND(CONCAT('{"id":"', REPLACE(STR(?child), "http://www.wikidata.org/entity/", ""), '"}') AS ?childJson)
                    }

                    # ---------------- FATHERS ----------------
                    OPTIONAL {
                        ?person wdt:P22 ?father .
                        BIND(CONCAT('{"id":"', REPLACE(STR(?father), "http://www.wikidata.org/entity/", ""), '"}') AS ?fatherJson)
                    }

                    # ---------------- MOTHERS ----------------
                    OPTIONAL {
                        ?person wdt:P25 ?mother .
                        BIND(CONCAT('{"id":"', REPLACE(STR(?mother), "http://www.wikidata.org/entity/", ""), '"}') AS ?motherJson)
                    }

                    # ---------------- SPOUSES ----------------
                    OPTIONAL {
                        ?person p:P26 ?s .
                        ?s ps:P26 ?spouse .
                        OPTIONAL { ?s pq:P580 ?startTime }
                        OPTIONAL { ?s pq:P582 ?endTime }
                        BIND(CONCAT(
                            '{"id":"',    REPLACE(STR(?spouse), "http://www.wikidata.org/entity/", ""),
                            '","start":"', COALESCE(STR(?startTime), ""),
                            '","end":"',   COALESCE(STR(?endTime), ""), '"}'
                        ) AS ?spouseJson)
                    }

                    SERVICE wikibase:label {
                        bd:serviceParam wikibase:language "en" .
                        ?person      rdfs:label ?personLabel .
                        ?birthCountry rdfs:label ?birthCountryLabel .
                    }
                }
                GROUP BY ?person ?personLabel ?birthDate ?deathDate ?image ?height ?wikipedia ?birthCountry ?birthCountryLabel
                """, wikidataId);
    }

    private Map<String, List<Relative>> parseRelatives(JsonNode row) {
        Map<String, List<Relative>> relatives = new HashMap<>();
        relatives.put("children", parseRelativeList(row, "children"));
        relatives.put("father",   parseRelativeList(row, "fathers"));
        relatives.put("mother",   parseRelativeList(row, "mothers"));
        relatives.put("spouses",  parseRelativeList(row, "spouses"));
        return relatives;
    }

    private List<Relative> parseRelativeList(JsonNode row, String field) {
        String raw = row.path(field).path("value").asString();
        String json = "[" + (raw != null ? raw : "") + "]";
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Relative.class));
    }

    private List<Person> fetchRelativePersons(Map<String, List<Relative>> relatives) {
        String relativeIds = relatives.values().stream()
                .flatMap(List::stream)
                .map(r -> "wd:" + r.id)
                .distinct()
                .collect(Collectors.joining(" "));

        if (relativeIds.isBlank()) {
            return List.of();
        }

        String sparql = String.format("""
                SELECT
                    ?person ?personLabel ?birthDate ?deathDate ?image ?height ?wikipedia ?birthCountry ?birthCountryLabel
                WHERE {
                    VALUES ?person { %s }

                    OPTIONAL { ?person wdt:P569 ?birthDate }
                    OPTIONAL { ?person wdt:P570 ?deathDate }
                    OPTIONAL { ?person wdt:P18  ?image }
                    OPTIONAL { ?person wdt:P2048 ?height }

                    OPTIONAL {
                        ?person wdt:P19 ?birthPlace .
                        ?birthPlace wdt:P17 ?birthCountry .
                    }

                    OPTIONAL {
                        ?article <http://schema.org/about> ?person ;
                                 <http://schema.org/isPartOf> <https://en.wikipedia.org/> .
                        BIND(?article AS ?wikipedia)
                    }

                    SERVICE wikibase:label {
                        bd:serviceParam wikibase:language "en" .
                        ?person       rdfs:label ?personLabel .
                        ?birthCountry rdfs:label ?birthCountryLabel .
                    }
                }
                """, relativeIds);

        return fetchData(sparql).valueStream().map(this::buildPersonFromProperties).toList();
    }

    private Person findExactlyOne(List<Person> persons, String wikidataId) {
        List<Person> matches = persons.stream()
                .filter(p -> p.getWikidataId().equals(wikidataId))
                .toList();
        if (matches.isEmpty()) {
            throw new IllegalStateException("Relative person not found in fetched results: " + wikidataId);
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Multiple persons found with wikidataId: " + wikidataId);
        }
        return matches.get(0);
    }

    private ExpandedPerson assembleExpandedPerson(Person person, Map<String, List<Relative>> relatives, List<Person> relativePersons) {
        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null");
        }

        List<FamilyRelationship> children = relatives.get("children").stream()
                .map(r -> new FamilyRelationship(null, findExactlyOne(relativePersons, r.id)))
                .toList();

        relatives.get("father").stream().findFirst()
                .ifPresent(r -> person.addParent(findExactlyOne(relativePersons, r.id)));

        relatives.get("mother").stream().findFirst()
                .ifPresent(r -> person.addParent(findExactlyOne(relativePersons, r.id)));

        List<MarriedTo> spouses = relatives.get("spouses").stream()
                .map(r -> new MarriedTo(null, findExactlyOne(relativePersons, r.id), r.start, r.end))
                .toList();
        person.setSpouses(spouses);

        return new ExpandedPerson(person, children);
    }

    private Person buildPersonFromProperties(JsonNode properties) {
        String personUrl = properties.path("person").path("value").asString();
        if (personUrl == null || personUrl.isEmpty()) {
            throw new IllegalArgumentException("Person URL cannot be null or empty");
        }

        String countryUrl = properties.path("birthCountry").path("value").asString();
        Country country = countryUrl != null ? Country.builder()
                .name(properties.path("birthCountryLabel").path("value").asString())
                .wikidataId(getWikidataIdFromUrl(countryUrl))
                .build() : null;

        return Person.builder()
                .name(properties.path("personLabel").path("value").asString())
                .birthdate(parseWikidataDate(properties.path("birthDate").path("value").asString()))
                .deathdate(parseWikidataDate(properties.path("deathDate").path("value").asString()))
                .wikipediaLink(properties.path("wikipedia").path("value").asString())
                .wikidataId(getWikidataIdFromUrl(personUrl))
                .imageLink(properties.path("image").path("value").asString())
                .bornIn(country)
                .build();
    }

    private JsonNode fetchData(String sparqlQuery) {
        String response;
        try {
            response = wikidataWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sparql")
                            .queryParam("format", "json")
                            .queryParam("query", sparqlQuery)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch data from Wikidata", e);
        }
        if (response == null) {
            throw new IllegalStateException("Wikidata returned an empty response");
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("results").path("bindings");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Wikidata response", e);
        }
    }

    private String getWikidataIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private LocalDate parseWikidataDate(String wikidataDateString) {
        if (wikidataDateString == null || wikidataDateString.isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(wikidataDateString).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private static class Relative {
        public String id;

        @JsonDeserialize(using = WikidataLocalDateDeserializer.class)
        public LocalDate start;

        @JsonDeserialize(using = WikidataLocalDateDeserializer.class)
        public LocalDate end;
    }
}
