package com.richardos17.family_tree.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WikidataWebClientConfig {

    @Bean
    public WebClient wikidataWebClient(
            @Value("${wikidata.sparql.base-url}") String baseUrl,
            @Value("${wikidata.sparql.user-agent}") String userAgent) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }
}
