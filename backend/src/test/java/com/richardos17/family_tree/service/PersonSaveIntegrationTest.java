package com.richardos17.family_tree.service;

import com.richardos17.family_tree.domain.Country;
import com.richardos17.family_tree.domain.ExpandedPerson;
import com.richardos17.family_tree.domain.FamilyRelationship;
import com.richardos17.family_tree.domain.MarriedTo;
import com.richardos17.family_tree.domain.Person;
import com.richardos17.family_tree.repository.CountryRepository;
import com.richardos17.family_tree.repository.PersonRepository;
import com.richardos17.family_tree.repository.PersonRelationshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class PersonSaveIntegrationTest {

    // Shared across all tests in this class — started once, reused.
    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5")
            .withoutAuthentication();

    @DynamicPropertySource
    static void configureNeo4j(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "");
    }

    // FetchPerson uses WebClient to call Wikidata — mocked so tests stay offline.
    @MockitoBean
    FetchPerson fetchPerson;

    @Autowired
    PersonSaveService personSaveService;

    @Autowired
    PersonService personService;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    PersonRelationshipRepository personRelationshipRepository;

    @BeforeEach
    void cleanUp() {
        personRepository.deleteAll();
        countryRepository.deleteAll();
    }

    // ── PersonSaveService: basic field persistence ────────────────────────────

    @Test
    void savePerson_allScalarFields_persistedCorrectly() {
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

    @Test
    void savePerson_setsRelationshipsExpandedFalse() {
        Person person = Person.builder().wikidataId("Q937").name("Albert Einstein").build();

        Person saved = personSaveService.savePerson(person);

        assertThat(saved.getRelationshipsExpanded()).isFalse();
        assertThat(personRepository.findByWikidataId("Q937")
                .map(Person::getRelationshipsExpanded).orElseThrow()).isFalse();
    }

    // ── PersonSaveService: country (BORN_IN relationship) ────────────────────

    @Test
    void savePerson_withCountry_bornInRelationshipAndCountryNodePersisted() {
        Country germany = Country.builder().wikidataId("Q183").name("Germany").build();
        Person person = Person.builder()
                .wikidataId("Q937")
                .name("Albert Einstein")
                .bornIn(germany)
                .build();

        personSaveService.savePerson(person);

        Person saved = personRepository.findByWikidataId("Q937").orElseThrow();
        assertThat(saved.getBornIn()).isNotNull();
        assertThat(saved.getBornIn().getWikidataId()).isEqualTo("Q183");
        assertThat(saved.getBornIn().getName()).isEqualTo("Germany");
        assertThat(countryRepository.existsByWikidataId("Q183")).isTrue();
    }

    @Test
    void savePerson_withoutCountry_noCountryNodeCreated() {
        Person person = Person.builder().wikidataId("Q937").name("Albert Einstein").build();

        personSaveService.savePerson(person);

        assertThat(countryRepository.count()).isZero();
    }

    @Test
    void savePerson_twoPeopleWithSameCountry_onlyOneCountryNodeInDb() {
        Country germany = Country.builder().wikidataId("Q183").name("Germany").build();
        Person einstein = Person.builder().wikidataId("Q937").name("Albert Einstein")
                .bornIn(germany).build();
        Person planck = Person.builder().wikidataId("Q9021").name("Max Planck")
                .bornIn(Country.builder().wikidataId("Q183").name("Germany").build()).build();

        personSaveService.savePerson(einstein);
        personSaveService.savePerson(planck);

        assertThat(countryRepository.count()).isEqualTo(1);
    }

    // ── PersonSaveService: parent/child relationships (HAS_PARENT) ───────────

    @Test
    void savePerson_withParent_hasParentRelationshipPersisted() {
        Person father = personSaveService.savePerson(
                Person.builder().wikidataId("Q100").name("Hermann Einstein").build());

        Person son = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        son.addParent(father);
        personSaveService.savePerson(son);

        Person savedSon = personRepository.findFullByWikidataId("Q937").orElseThrow();
        assertThat(savedSon.getParents()).hasSize(1);
        assertThat(savedSon.getParents().get(0).getEntity().getWikidataId()).isEqualTo("Q100");
    }

    @Test
    void savePerson_parentNotYetInDb_parentSavedAutomatically() {
        Person father = Person.builder().wikidataId("Q100").name("Hermann Einstein").build();
        Person son = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        son.addParent(father);

        personSaveService.savePerson(son);

        assertThat(personRepository.findByWikidataId("Q100")).isPresent();
        assertThat(personRepository.count()).isEqualTo(2);
    }

    // ── PersonSaveService: idempotency ────────────────────────────────────────

    @Test
    void savePerson_calledTwiceWithSameWikidataId_nodesNotDuplicated() {
        personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());
        personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());

        assertThat(personRepository.count()).isEqualTo(1);
    }

    // ── PersonSaveService: expanded person ───────────────────────────────────

    @Test
    void saveExpandedPerson_setsRelationshipsExpandedTrue() {
        ExpandedPerson expanded = new ExpandedPerson(
                Person.builder().wikidataId("Q937").name("Albert Einstein").build(),
                List.of());

        personSaveService.saveExpandedPerson(expanded);

        assertThat(personRepository.findByWikidataId("Q937")
                .map(Person::getRelationshipsExpanded).orElseThrow()).isTrue();
    }

    @Test
    void saveExpandedPerson_withChildren_childHasParentRelationshipPersisted() {
        Person parent = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        Person child = Person.builder().wikidataId("Q60197").name("Hans Albert Einstein").build();
        ExpandedPerson expanded = new ExpandedPerson(parent, List.of(new FamilyRelationship(null, child)));

        personSaveService.saveExpandedPerson(expanded);

        Person savedChild = personRepository.findFullByWikidataId("Q60197").orElseThrow();
        assertThat(savedChild.getParents()).hasSize(1);
        assertThat(savedChild.getParents().get(0).getEntity().getWikidataId()).isEqualTo("Q937");
    }

    @Test
    void saveExpandedPerson_withChildren_bothNodesInDb() {
        Person parent = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        Person child = Person.builder().wikidataId("Q60197").name("Hans Albert Einstein").build();

        personSaveService.saveExpandedPerson(new ExpandedPerson(parent, List.of(new FamilyRelationship(null, child))));

        assertThat(personRepository.count()).isEqualTo(2);
    }

    // ── PersonSaveService: validation ────────────────────────────────────────

    @Test
    void savePerson_null_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> personSaveService.savePerson(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Person is null");
    }

    @Test
    void savePerson_missingWikidataId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> personSaveService.savePerson(Person.builder().name("No ID").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wikidata ID is required");
    }

    @Test
    void saveExpandedPerson_null_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> personSaveService.saveExpandedPerson(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expanded person is null");
    }

    // ── PersonService: fetch + save flow (FetchPerson mocked) ────────────────

    @Test
    void getPersonsByName_notInDb_mockedFetchResultIsSavedToDb() {
        Person fetched = Person.builder()
                .wikidataId("Q937")
                .name("Albert Einstein")
                .birthdate(LocalDate.of(1879, 3, 14))
                .build();
        when(fetchPerson.fetchPersonsByName("Einstein")).thenReturn(List.of(fetched));

        personService.getPersonsByName("Einstein");

        Optional<Person> saved = personRepository.findByWikidataId("Q937");
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Albert Einstein");
        assertThat(saved.get().getBirthdate()).isEqualTo(LocalDate.of(1879, 3, 14));
    }

    @Test
    void getPersonsByName_alreadyInDb_returnedFromDbWithoutCallingFetch() {
        personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());

        List<Person> result = personService.getPersonsByName("Albert");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWikidataId()).isEqualTo("Q937");
        // fetchPerson.fetchPersonsByName never called — Mockito raises no-interaction failure if it were
    }

    @Test
    void getPersonByWikidataId_notInDb_mockedFetchResultIsSavedToDb() {
        when(fetchPerson.fetchPersonByWikidataId("Q937"))
                .thenReturn(Person.builder().wikidataId("Q937").name("Albert Einstein").build());

        Optional<Person> result = personService.getPersonByWikidataId("Q937");

        assertThat(result).isPresent();
        assertThat(personRepository.findByWikidataId("Q937")).isPresent();
    }

    @Test
    void getPersonByWikidataId_alreadyInDb_returnedWithoutCallingFetch() {
        personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());

        Optional<Person> result = personService.getPersonByWikidataId("Q937");

        assertThat(result).isPresent();
        assertThat(result.get().getWikidataId()).isEqualTo("Q937");
    }

    // ── Relationships: MARRIED_TO ─────────────────────────────────────────────

    @Test
    void savePerson_withSpouse_marriedToRelationshipPersisted() {
        Person mileva = personSaveService.savePerson(
                Person.builder().wikidataId("Q60212").name("Mileva Marić").build());

        Person einstein = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        einstein.addMarriage(mileva, null, null);
        personSaveService.savePerson(einstein);

        // findFullByWikidataId loads spouses via collect(DISTINCT rM) / collect(DISTINCT spouse)
        Person saved = personRepository.findFullByWikidataId("Q937").orElseThrow();
        assertThat(saved.getSpouses()).hasSize(1);
        assertThat(saved.getSpouses().get(0).getSpouse().getWikidataId()).isEqualTo("Q60212");
    }

    @Test
    void savePerson_withMarriageDates_datesPersistedOnRelationship() {
        Person mileva = personSaveService.savePerson(
                Person.builder().wikidataId("Q60212").name("Mileva Marić").build());

        Person einstein = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        einstein.addMarriage(mileva, LocalDate.of(1903, 1, 6), LocalDate.of(1919, 2, 14));
        personSaveService.savePerson(einstein);

        Person saved = personRepository.findFullByWikidataId("Q937").orElseThrow();
        MarriedTo marriage = saved.getSpouses().get(0);
        assertThat(marriage.getStartDate()).isEqualTo(LocalDate.of(1903, 1, 6));
        assertThat(marriage.getEndDate()).isEqualTo(LocalDate.of(1919, 2, 14));
    }

    @Test
    void savePerson_spouseNotYetInDb_spouseSavedAutomatically() {
        Person mileva = Person.builder().wikidataId("Q60212").name("Mileva Marić").build();
        Person einstein = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        einstein.addMarriage(mileva, null, null);

        personSaveService.savePerson(einstein);

        assertThat(personRepository.findByWikidataId("Q60212")).isPresent();
        assertThat(personRepository.count()).isEqualTo(2);
    }

    @Test
    void savePerson_withMultipleSpouses_allMarriagesPersisted() {
        Person mileva = personSaveService.savePerson(
                Person.builder().wikidataId("Q60212").name("Mileva Marić").build());
        Person elsa = personSaveService.savePerson(
                Person.builder().wikidataId("Q72965").name("Elsa Einstein").build());

        Person einstein = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        einstein.addMarriage(mileva, LocalDate.of(1903, 1, 6), LocalDate.of(1919, 2, 14));
        einstein.addMarriage(elsa, LocalDate.of(1919, 6, 2), null);
        personSaveService.savePerson(einstein);

        Person saved = personRepository.findFullByWikidataId("Q937").orElseThrow();
        assertThat(saved.getSpouses()).hasSize(2);
        assertThat(saved.getSpouses())
                .extracting(m -> m.getSpouse().getWikidataId())
                .containsExactlyInAnyOrder("Q60212", "Q72965");
    }

    @Test
    void savePerson_withSpouseAndParent_bothRelationshipsPersisted() {
        Person mother = personSaveService.savePerson(
                Person.builder().wikidataId("Q200").name("Pauline Einstein").build());
        Person mileva = personSaveService.savePerson(
                Person.builder().wikidataId("Q60212").name("Mileva Marić").build());

        Person einstein = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        einstein.addParent(mother);
        einstein.addMarriage(mileva, null, null);
        personSaveService.savePerson(einstein);

        Person saved = personRepository.findFullByWikidataId("Q937").orElseThrow();
        assertThat(saved.getParents()).hasSize(1);
        assertThat(saved.getParents().get(0).getEntity().getWikidataId()).isEqualTo("Q200");
        assertThat(saved.getSpouses()).hasSize(1);
        assertThat(saved.getSpouses().get(0).getSpouse().getWikidataId()).isEqualTo("Q60212");
    }

    @Test
    void findSpousesByPersonId_returnsSpouseLoadedViaCustomRepository() {
        Person mileva = personSaveService.savePerson(
                Person.builder().wikidataId("Q60212").name("Mileva Marić").build());
        Person einstein = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        einstein.addMarriage(mileva, LocalDate.of(1903, 1, 6), null);
        personSaveService.savePerson(einstein);

        List<MarriedTo> spouses = personRelationshipRepository.findSpousesByPersonId("Q937");

        assertThat(spouses).hasSize(1);
        assertThat(spouses.get(0).getSpouse().getWikidataId()).isEqualTo("Q60212");
        assertThat(spouses.get(0).getStartDate()).isEqualTo(LocalDate.of(1903, 1, 6));
    }

    @Test
    void findParentsByChildId_returnsParentsLoadedViaCustomRepository() {
        Person father = personSaveService.savePerson(
                Person.builder().wikidataId("Q100").name("Hermann Einstein").build());
        Person son = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        son.addParent(father);
        personSaveService.savePerson(son);

        List<FamilyRelationship> parents = personRelationshipRepository.findParentsByChildId("Q937");

        assertThat(parents).hasSize(1);
        assertThat(parents.get(0).getEntity().getWikidataId()).isEqualTo("Q100");
    }

    @Test
    void findChildrenByParentId_returnsChildrenLoadedViaCustomRepository() {
        Person parent = Person.builder().wikidataId("Q937").name("Albert Einstein").build();
        Person child = Person.builder().wikidataId("Q60197").name("Hans Albert Einstein").build();
        personSaveService.saveExpandedPerson(new ExpandedPerson(parent, List.of(new FamilyRelationship(null, child))));

        List<FamilyRelationship> children = personRelationshipRepository.findChildrenByParentId("Q937");

        assertThat(children).hasSize(1);
        assertThat(children.get(0).getEntity().getWikidataId()).isEqualTo("Q60197");
    }
}