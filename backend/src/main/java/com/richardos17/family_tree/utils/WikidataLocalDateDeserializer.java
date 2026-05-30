package com.richardos17.family_tree.utils;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;


import java.time.LocalDate;
import java.time.ZonedDateTime;

public class WikidataLocalDateDeserializer extends StdDeserializer<LocalDate> {

    public WikidataLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) 
            {
        String dateString = p.getValueAsString();
        return parseWikidataDate(dateString);
    }

    /**
     * Parses Wikidata ISO 8601 formatted dates (with time and timezone)
     * into LocalDate by extracting only the date portion.
     * This is the same logic as FetchPerson.parseWikidataDate()
     *
     * @param wikidataDateString ISO 8601 formatted date string (e.g., "1879-03-14T00:00:00Z")
     * @return LocalDate object, or null if the input is null, empty, or invalid
     */
    private LocalDate parseWikidataDate(String wikidataDateString) {
        if (wikidataDateString == null || wikidataDateString.isEmpty()) {
            return null;
        }
    
        try {
            // Parse as ZonedDateTime to handle timezone information, then extract date
            return ZonedDateTime.parse(wikidataDateString).toLocalDate();
        } catch (Exception e) {
            // Fallback: return null for invalid dates (like URLs) instead of throwing exception
            return null;
        }
    }
}
