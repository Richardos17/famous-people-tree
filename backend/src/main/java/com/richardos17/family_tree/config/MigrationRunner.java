package com.richardos17.family_tree.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
