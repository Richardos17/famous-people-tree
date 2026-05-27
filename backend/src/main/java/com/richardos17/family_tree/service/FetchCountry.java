package com.richardos17.family_tree.service;

import com.richardos17.family_tree.domain.Country;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Service
public class FetchCountry {
       private final WebClient webClient;

    public static void main(String[] args) {
        FetchCountry fetchCountry = new FetchCountry();
        System.out.println( fetchCountry.fetchCountryByWikidataId("Q317521"));
    }
    public FetchCountry() {
        webClient = WebClient.builder()
                .baseUrl("https://query.wikidata.org")
                .defaultHeader("User-Agent", "FamilyTreeDemo/1.0 (your@email.com)")
                .build();
    }
   public Country fetchCountryByWikidataId(String wikidataId) {


        String sparql = String.format("""
            SELECT
                              ?country
                              ?countryLabel
                            WHERE {

                              BIND(wd:%s AS ?country)

                              SERVICE wikibase:label {
                                bd:serviceParam wikibase:language "en" .
                              }
                            }
            """, wikidataId);
        URI targetUri = UriComponentsBuilder.fromUriString("https://query.wikidata.org/sparql")
                .queryParam("format", "json")
                .queryParam("query", sparql)
                .build()
                .toUri();

        String response = webClient.get()
                .uri(targetUri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode properties = root.path("results").path("bindings").get(0);

       return Country.builder()
               .name(properties.get("countryLabel").path("value").asText())
               .wikidataId(wikidataId)
               .build();
    }



}
