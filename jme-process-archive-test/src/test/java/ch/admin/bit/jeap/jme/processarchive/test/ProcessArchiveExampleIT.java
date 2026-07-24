package ch.admin.bit.jeap.jme.processarchive.test;

import ch.admin.bit.jeap.jme.test.BootServiceSpringIntegrationTestBase;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Starts the real, packaged jme-process-archive-auth-scs, jme-process-archive-resource-service,
 * jme-process-archive-service (the PAS instance) and jme-process-archive-inspection-service against the
 * docker-compose infrastructure (real Kafka, PostgreSQL, RustFS S3 and Vault), issues a decree through the
 * resource service's REST API, and verifies that the process archive service archived it by reading it back
 * through the inspection service.
 */
class ProcessArchiveExampleIT extends BootServiceSpringIntegrationTestBase {

    private static final String AUTH_BASE_URL = "http://localhost:8081/jme-process-archive-auth-scs";
    private static final String RESOURCE_BASE_URL = "http://localhost:8082/jme-process-archive-resource-service";
    private static final String PAS_BASE_URL = "http://localhost:8080/process-archive";
    private static final String INSPECTION_BASE_URL = "http://localhost:8083/jme-process-archive-inspection-service";

    private static final String ARCHIVE_BUCKET = "bit-jme-processarchive-lockable-obs-local";

    @BeforeAll
    static void startServices() throws Exception {
        startService("jme-process-archive-auth-scs", AUTH_BASE_URL);
        startService("jme-process-archive-resource-service", RESOURCE_BASE_URL);
        startService("jme-process-archive-service", PAS_BASE_URL);
        startService("jme-process-archive-inspection-service", INSPECTION_BASE_URL);

        // The health checks above only confirm the HTTP servers and databases are up - not that the PAS's Kafka
        // consumer group has finished its initial rebalance. Since it uses the default auto.offset.reset=latest
        // on a fresh consumer group, a decree issued before the rebalance completes would be silently skipped.
        KafkaConsumerGroupAwaiter.waitForAssignment("jme-process-archive-service", "jme-process-archive-decreecreated");
    }

    @Test
    void decreeIssuedByResourceServiceIsArchivedAndReadableViaInspectionService() {
        String title = "it-test-decree-" + UUID.randomUUID();

        Map<String, Object> decree = given()
                .baseUri(RESOURCE_BASE_URL)
                .contentType("application/json")
                .body(Map.of("title", title, "someDecreeData", "example data"))
                .when()
                .post("/api/decrees")
                .then()
                .statusCode(200)
                .extract().jsonPath().getMap("$");

        assertThat(decree).containsKeys("decreeId", "decreeDocumentId");
        String decreeId = (String) decree.get("decreeId");

        await().untilAsserted(() -> {
            List<Map<String, String>> objects = listArchivedObjects();
            assertThat(objects)
                    .anySatisfy(object -> assertThat(object.get("reference-id")).isEqualTo(decreeId));
        });
    }

    private List<Map<String, String>> listArchivedObjects() {
        return given()
                .baseUri(INSPECTION_BASE_URL)
                .when()
                .get("/api/archived-data/{bucket}/list", ARCHIVE_BUCKET)
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<>() {});
    }
}
