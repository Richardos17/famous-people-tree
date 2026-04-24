package com.richardos17.family_tree.config;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Profile("db-refresh")
@Component
public class MigrationRunner implements CommandLineRunner {

    private final Neo4jClient neo4jClient;
    private final ResourcePatternResolver resourceResolver;

    public MigrationRunner(Neo4jClient neo4jClient, ResourceLoader resourceLoader) {
        this.neo4jClient = neo4jClient;
        this.resourceResolver = (ResourcePatternResolver) resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {


        System.out.println("Running migrations...");

        // DROP CONSTRAINTS
        List<Map<String, Object>> constraints = (List<Map<String, Object>>) neo4jClient.query("""
                SHOW CONSTRAINTS YIELD name
                RETURN name
                """)
                .fetch()
                .all();

        for (Map<String, Object> row : constraints) {
            String name = (String) row.get("name");

            neo4jClient.query("DROP CONSTRAINT `" + name + "` IF EXISTS")
                    .run();

            System.out.println("Dropped constraint: " + name);
        }
        // DROP INDEXES
        List<Map<String, Object>> indexes = (List<Map<String, Object>>) neo4jClient.query("""
                SHOW INDEXES YIELD name, type
                WHERE type <> 'LOOKUP'
                RETURN name
                """)
                .fetch()
                .all();

        for (Map<String, Object> row : indexes) {
            String name = (String) row.get("name");

            neo4jClient.query("DROP INDEX `" + name + "` IF EXISTS")
                    .run();

            System.out.println("Dropped index: " + name);
        }
        Resource[] resources = resourceResolver.getResources(
                "classpath:db/migrations/*.cypher"
        );

        List<Resource> sorted = Arrays.stream(resources)
                .sorted(Comparator.comparing(Resource::getFilename))
                .toList();

        for (Resource resource : sorted) {

            String filename = resource.getFilename(); // V1__init_schema.cypher
            String version = filename.split("__")[0];

//            Boolean exists = neo4jClient.query("""
//                    MATCH (m:Migration {version: $version})
//                    RETURN count(m) > 0 AS exists
//                    """)
//                    .bind(version).to("version")
//                    .fetchAs(Boolean.class)
//                    .one()
//                    .orElse(false);
//
//            if (exists) {
//                continue;
//            }

            String cypher = new String(resource.getInputStream().readAllBytes());

            neo4jClient.query(cypher).run();

//            neo4jClient.query("""
//                    CREATE (:Migration {
//                        version: $version,
//                        appliedAt: datetime()
//                    })
//                    """)
//                    .bind(version).to("version")
//                    .run();

            System.out.println("Applied migration: " + version);
        }
    }
}
