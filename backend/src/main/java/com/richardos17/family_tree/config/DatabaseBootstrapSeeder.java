package com.richardos17.family_tree.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;
@Profile("db-refresh")
@Component
public class DatabaseBootstrapSeeder implements CommandLineRunner {

    private final Neo4jClient neo4jClient;

    public DatabaseBootstrapSeeder(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public void run(String... args) {

        boolean isEmpty = neo4jClient.query("""
                MATCH (n)
                RETURN count(n) = 0 AS empty
                """)
                .fetchAs(Boolean.class)
                .one()
                .orElse(true);

        if (!isEmpty) {
            System.out.println("DB already contains data → skipping bootstrap");
            return;
        }

        System.out.println("DB is empty → seeding initial data");

        seed();
    }

    @SuppressWarnings("checkstyle:MethodLength")
    private void seed() {

        neo4jClient.query("""
                MERGE (usa:Country {id:"C1", name:"United States"})
                MERGE (uk:Country {id:"C2", name:"United Kingdom"})
                MERGE (fr:Country {id:"C3", name:"France"})
                MERGE (de:Country {id:"C4", name:"Germany"})
                
                MERGE (john:Person {
                  id:"P1",
                  name:"John Adams",
                  birthdate:date("1940-02-10"),
                  deathdate:date("2010-06-01"),
                  image_link:"https://example.com/john.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/John_Adams_(fictional)",
                  height:180
                })
                
                MERGE (mary:Person {
                  id:"P2",
                  name:"Mary Adams",
                  birthdate:date("1945-05-21"),
                  deathdate:date("2018-09-12"),
                  image_link:"https://example.com/mary.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Mary_Adams",
                  height:165
                })
                
                MERGE (john)-[:BORN_IN]->(usa)
                MERGE (mary)-[:BORN_IN]->(uk)
                
                MERGE (john)-[:CITIZEN_OF {start_date:date("1940-01-01"), end_date:date("2010-06-01")}]->(usa)
                MERGE (mary)-[:CITIZEN_OF {start_date:date("1945-05-21"), end_date:date("2018-09-12")}]->(uk)
                
                MERGE (john)-[:MARRIED_TO {start_date:date("1965-01-01"), end_date:date("2010-06-01"), source:"bootstrap"}]-(mary)
               
                
                MERGE (peter:Person {
                  id:"P3",
                  name:"Peter Adams",
                  birthdate:date("1968-03-14"),
                  image_link:"https://example.com/peter.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Peter_Adams",
                  height:182
                })
                
                MERGE (lisa:Person {
                  id:"P4",
                  name:"Lisa Adams",
                  birthdate:date("1970-11-02"),
                  image_link:"https://example.com/lisa.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Lisa_Adams",
                  height:170
                })
                
                MERGE (peter)-[:BORN_IN]->(usa)
                MERGE (lisa)-[:BORN_IN]->(uk)
                
                MERGE (peter)-[:CITIZEN_OF {start_date:date("1968-03-14"), end_date:date("2024-12-31")}]->(usa)
                MERGE (lisa)-[:CITIZEN_OF {start_date:date("1970-11-02"), end_date:date("2024-12-31")}]->(uk)
                
                MERGE (peter)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(john)
                MERGE (peter)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(mary)
                
                MERGE (lisa)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(john)
                MERGE (lisa)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(mary)
                
                MERGE (anna:Person {
                  id:"P5",
                  name:"Anna Müller",
                  birthdate:date("1972-07-19"),
                  image_link:"https://example.com/anna.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Anna_Muller",
                  height:168
                })
                
                MERGE (peter)-[:MARRIED_TO {start_date:date("1995-06-01"), source:"bootstrap"}]-(anna)
               
                
                MERGE (anna)-[:BORN_IN]->(de)
                MERGE (anna)-[:CITIZEN_OF {start_date:date("1972-07-19"), end_date:date("2024-12-31")}]->(de)
                
                MERGE (emma:Person {
                  id:"P6",
                  name:"Emma Adams",
                  birthdate:date("1998-01-12"),
                  image_link:"https://example.com/emma.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Emma_Adams",
                  height:172
                })
                
                MERGE (noah:Person {
                  id:"P7",
                  name:"Noah Adams",
                  birthdate:date("2001-06-25"),
                  image_link:"https://example.com/noah.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Noah_Adams",
                  height:178
                })
                
                MERGE (emma)-[:BORN_IN]->(de)
                MERGE (noah)-[:BORN_IN]->(de)
                
                MERGE (emma)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(peter)
                MERGE (emma)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(anna)
                
                MERGE (noah)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(peter)
                MERGE (noah)-[:HAS_PARENT {type:"biological", source:"bootstrap"}]->(anna)
                
                MERGE (emma)-[:CITIZEN_OF {start_date:date("1998-01-12"), end_date:date("2024-12-31")}]->(de)
                MERGE (noah)-[:CITIZEN_OF {start_date:date("2001-06-25"), end_date:date("2024-12-31")}]->(de)
                
                MERGE (lucas:Person {
                  id:"P8",
                  name:"Lucas Martin",
                  birthdate:date("1997-09-09"),
                  image_link:"https://example.com/lucas.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Lucas_Martin",
                  height:175
                })
                
                MERGE (sophie:Person {
                  id:"P9",
                  name:"Sophie Martin",
                  birthdate:date("1999-12-30"),
                  image_link:"https://example.com/sophie.jpg",
                  wikipedia_link:"https://en.wikipedia.org/wiki/Sophie_Martin",
                  height:166
                })
                
                MERGE (lucas)-[:BORN_IN]->(fr)
                MERGE (sophie)-[:BORN_IN]->(fr)
                
                MERGE (lucas)-[:CITIZEN_OF {start_date:date("1997-09-09"), end_date:date("2024-12-31")}]->(fr)
                MERGE (sophie)-[:CITIZEN_OF {start_date:date("1999-12-30"), end_date:date("2024-12-31")}]->(fr)
                """).run();

        System.out.println("Bootstrap seeding completed");
    }
}
