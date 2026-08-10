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
import java.util.ArrayList;
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

        Person son = personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());
        personSaveService.saveFamilyRelationship(father.getLocalId(), son.getLocalId());

        List<FamilyRelationship> savedChildParents = personRelationshipRepository.findParentsByChildId("Q937");
        assertThat(savedChildParents).hasSize(1);
        assertThat(savedChildParents.getFirst().getEntity()).isEqualTo(father);

        List<FamilyRelationship> savedFatherChildren = personRelationshipRepository.findChildrenByParentId("Q100");
        assertThat(savedFatherChildren).hasSize(1);
        assertThat(savedFatherChildren.getFirst().getEntity()).isEqualTo(son);
    }

    // ── PersonSaveService: idempotency ────────────────────────────────────────

    @Test
    void savePerson_calledTwiceWithSameWikidataId_nodesNotDuplicated() {
        personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());
        personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());

        assertThat(personRepository.count()).isEqualTo(1);
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


    // ── Relationships: MARRIED_TO ─────────────────────────────────────────────

    @Test
    void savePerson_withSpouse_marriedToRelationshipPersisted() {
        Person mileva = personSaveService.savePerson(
                Person.builder().wikidataId("Q60212").name("Mileva Marić").build());

        Person einstein = personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());
        personSaveService.saveMarriedRelationship(mileva.getLocalId(), einstein.getLocalId(), null, null);

        List<MarriedTo> saved = personRelationshipRepository.findSpousesByPersonId("Q937");
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getSpouse()).isEqualTo(mileva);
    }

    @Test
    void savePerson_withMarriageDates_datesPersistedOnRelationship() {
        Person mileva = personSaveService.savePerson(
                Person.builder().wikidataId("Q60212").name("Mileva Marić").build());

        Person einstein = personSaveService.savePerson(Person.builder().wikidataId("Q937").name("Albert Einstein").build());
        personSaveService.saveMarriedRelationship(mileva.getLocalId(),
                einstein.getLocalId(), LocalDate.of(1903, 1, 6), LocalDate.of(1919, 2, 14));

        List<MarriedTo> saved = personRelationshipRepository.findSpousesByPersonId("Q937");
        assertThat(saved).hasSize(1);
        MarriedTo marriage = saved.getFirst();
        assertThat(marriage.getStartDate()).isEqualTo(LocalDate.of(1903, 1, 6));
        assertThat(marriage.getEndDate()).isEqualTo(LocalDate.of(1919, 2, 14));
        assertThat(marriage.getSpouse()).isEqualTo(mileva);
    }
    /*
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

        List<MarriedTo> saved = personRelationshipRepository.findSpousesByPersonId("Q937");
        assertThat(saved).hasSize(2);
        assertThat(saved)
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

        List<MarriedTo> savedSpouses = personRelationshipRepository.findSpousesByPersonId("Q937");
        List<FamilyRelationship> savedChildParents = personRelationshipRepository.findParentsByChildId("Q100");

        assertThat(savedChildParents).hasSize(1);
        assertThat(savedChildParents.getFirst().getEntity().getWikidataId()).isEqualTo("Q200");
        assertThat(savedSpouses).hasSize(1);
        assertThat(savedSpouses.getFirst().getSpouse().getWikidataId()).isEqualTo("Q60212");
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
    }*/
}