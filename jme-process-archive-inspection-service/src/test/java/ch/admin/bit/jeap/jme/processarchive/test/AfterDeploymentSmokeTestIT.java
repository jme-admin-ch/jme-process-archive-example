package ch.admin.bit.jeap.jme.processarchive.test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpHeaders;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SameParameterValue")
@Slf4j
@EnabledIfSystemProperty(named = AfterDeploymentSmokeTestIT.DEPLOY_STAGE_PROPERTY_NAME, matches = "dev|local")
class AfterDeploymentSmokeTestIT {

    static final String DEPLOY_STAGE_PROPERTY_NAME = "deployStage";
    private static final String DEPLOY_PLATFORM_PROPERTY_NAME = "deployPlatform";

    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(1);

    private RequestSpecification resourceServiceRequest;
    private RequestSpecification inspectionServiceRequest;
    private RequestSpecification archiveServiceRequest;
    private String deployPlatform;

    private String authServerUrl;

    @Test
    void createBackfillJobAndReadReport() {
        String decreeDocumentId = createDecreeDocument();
        UUID jobId = UUID.randomUUID();
        String accessToken = retrieveAccessToken();
        String request = backfillRequestForDecreeDocument(decreeDocumentId);

        Response createResponse = given()
                .config(RestAssured.config.encoderConfig(encoderConfig()
                        .encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                .spec(archiveServiceRequest).auth().oauth2(accessToken)
                .contentType("application/yaml")
                .body(request)
                .when().put("/process-archive/api/jobs/" + jobId);
        assertThat(createResponse.statusCode()).isEqualTo(200);

        Response idempotentCreateResponse = given()
                .config(RestAssured.config.encoderConfig(encoderConfig()
                        .encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                .spec(archiveServiceRequest).auth().oauth2(accessToken)
                .contentType("application/yaml")
                .body(request)
                .when().put("/process-archive/api/jobs/" + jobId);
        assertThat(idempotentCreateResponse.statusCode()).isEqualTo(200);

        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> {
                    Response reportResponse = given()
                            .config(RestAssured.config.encoderConfig(encoderConfig()
                                    .encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                            .spec(archiveServiceRequest).auth().oauth2(accessToken)
                            .accept("application/yaml")
                            .when().get("/process-archive/api/jobs/" + jobId + "/report");
                    assertThat(reportResponse.statusCode()).isEqualTo(200);
                    assertThat(reportResponse.contentType()).contains("application/yaml");
                    assertThat(reportResponse.getBody().asString())
                            .contains("message: JmeDecreeDocumentCreatedEvent")
                            .contains("config-id: decree-document")
                            .contains("job-id: " + jobId)
                            .contains("job-state: completed")
                            .contains("job-result: succeeded")
                            .contains("archiveDataReferences:")
                            .contains("id: " + decreeDocumentId)
                            .contains("state: succeeded");
                });
    }

    @Test
    void createBackfillJobForDiagramAndReadReport() {
        DiagramInfo diagramInfo = createDiagram();
        UUID jobId = UUID.randomUUID();
        String accessToken = retrieveAccessToken();
        String request = backfillRequestForDiagram(diagramInfo.id(), diagramInfo.version());

        Response createResponse = given()
                .config(RestAssured.config.encoderConfig(encoderConfig()
                        .encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                .spec(archiveServiceRequest).auth().oauth2(accessToken)
                .contentType("application/yaml")
                .body(request)
                .when().put("/process-archive/api/jobs/" + jobId);
        assertThat(createResponse.statusCode()).isEqualTo(200);

        Response idempotentCreateResponse = given()
                .config(RestAssured.config.encoderConfig(encoderConfig()
                        .encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                .spec(archiveServiceRequest).auth().oauth2(accessToken)
                .contentType("application/yaml")
                .body(request)
                .when().put("/process-archive/api/jobs/" + jobId);
        assertThat(idempotentCreateResponse.statusCode()).isEqualTo(200);

        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> {
                    Response reportResponse = given()
                            .config(RestAssured.config.encoderConfig(encoderConfig()
                                    .encodeContentTypeAs("application/yaml", ContentType.TEXT)))
                            .spec(archiveServiceRequest).auth().oauth2(accessToken)
                            .accept("application/yaml")
                            .when().get("/process-archive/api/jobs/" + jobId + "/report");
                    assertThat(reportResponse.statusCode()).isEqualTo(200);
                    assertThat(reportResponse.contentType()).contains("application/yaml");
                    assertThat(reportResponse.getBody().asString())
                            .contains("message: JmeDiagramVersionCreatedEvent")
                            .contains("job-id: " + jobId)
                            .contains("job-state: completed")
                            .contains("job-result: succeeded")
                            .contains("archiveDataReferences:")
                            .contains("id: " + diagramInfo.id())
                            .contains("version: " + diagramInfo.version())
                            .contains("state: succeeded");
                });
    }

    @Test
    void getBackfillJobReportReturnsNotFoundForUnknownJob() {
        String accessToken = retrieveAccessToken();
        Response reportResponse = given().spec(archiveServiceRequest).auth().oauth2(accessToken)
                .accept("application/yaml")
                .when().get("/process-archive/api/jobs/" + UUID.randomUUID() + "/report");

        assertThat(reportResponse.statusCode()).isEqualTo(404);
    }

    @Test
    void archiveDecreeAndDecreeDocument() {

        // Data to archive
        final String decreeDataToArchive = UUID.randomUUID().toString();
        final String titleToArchive = UUID.randomUUID().toString();

        // Archive Decree and DecreeDocument and Get ids
        Response archiveResponse = given().spec(resourceServiceRequest).contentType(ContentType.JSON)
                .body("{\"title\":\"" + titleToArchive + "\",\"someDecreeData\":\"" + decreeDataToArchive + "\"}")
                .when().post("/jme-process-archive-resource-service/api/decrees");

        JsonPath jsonPathEvaluator = archiveResponse.jsonPath();
        String decreeId = jsonPathEvaluator.get("decreeId");
        String decreeDocumentId = jsonPathEvaluator.get("decreeDocumentId");
        assertThat(decreeId).isNotNull();
        assertThat(decreeDocumentId).isNotNull();

        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .until(() -> areArchivedObjectsPresent(Set.of(decreeId, decreeDocumentId)));

        // The JmeDecreeCreatedEvent leads to the archival of two artifacts (Decree and DecreeSummary) with the same
        // reference id (the decree id). Wait until the DecreeSummary object is present, too.
        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .until(() -> isArchivedObjectPresent(decreeId, "DecreeSummary"));

        // Check the archived data
        List<S3ObjectMetadata> s3Objects = getObjectMetadata();
        checkArchivedDecree(s3Objects, decreeId, decreeDataToArchive, titleToArchive);
        checkArchivedDecreeSummary(s3Objects, decreeId, titleToArchive);
        checkArchivedDecreeDocument(s3Objects, decreeDocumentId, decreeDataToArchive, titleToArchive);
        checkArchivedDecreeDocumentWithArchiveReader(s3Objects, decreeDocumentId);

        // Retrieve access token from mock server
        String accessToken = retrieveAccessToken();
        checkArchivedDecreeConverterToSearchItem(s3Objects, decreeId, titleToArchive, accessToken);
        checkArchivedDecreeDocumentConverterToSearchItem(s3Objects, decreeDocumentId, accessToken);

        // Check the SharedArchivedArtifactVersionCreatedEvent was produced with the correct variant
        // Wait for the SharedArchivedArtifactVersionCreatedEvent to be consumed
        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .until(() -> isSharedArchivedArtifactVersionCreatedEventPresent(decreeId));

        checkVariantOfSharedArchivedArtifactVersionCreatedEventPresent(decreeId, "JME_Decree");

        // The event for the second decree artifact may arrive slightly later than the first one
        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> checkVariantOfSharedArchivedArtifactVersionCreatedEventPresent(decreeId, "JME_DecreeSummary"));

        checkVariantOfSharedArchivedArtifactVersionCreatedEventPresent(decreeDocumentId, "JME_DecreeDocument");

    }

    private String createDecreeDocument() {
        Response archiveResponse = given().spec(resourceServiceRequest).contentType(ContentType.JSON)
                .body("{\"title\":\"backfill-title-" + UUID.randomUUID() + "\",\"someDecreeData\":\"backfill-data\"}")
                .when().post("/jme-process-archive-resource-service/api/decrees");

        assertThat(archiveResponse.statusCode()).isEqualTo(200);
        String decreeDocumentId = archiveResponse.jsonPath().get("decreeDocumentId");
        assertThat(decreeDocumentId).isNotNull();
        return decreeDocumentId;
    }

    private String backfillRequestForDecreeDocument(String decreeDocumentId) {
        // config-id is optional here (single remote-data configuration), but exercises addressing a
        // specific archive configuration by its id
        return """
                message: JmeDecreeDocumentCreatedEvent
                config-id: decree-document
                archiveDataReferences:
                  - id: %s
                """.formatted(decreeDocumentId);
    }

    private record DiagramInfo(String id, int version) {}

    private DiagramInfo createDiagram() {
        String diagramId = UUID.randomUUID().toString();
        Response archiveResponse = given().spec(resourceServiceRequest).contentType(ContentType.JSON)
                .body("{\"name\":\"backfill-diagram\",\"graph\":\"backfill-graph\"}")
                .when().put("/jme-process-archive-resource-service/api/diagrams/" + diagramId);

        assertThat(archiveResponse.statusCode()).isEqualTo(200);
        String id = archiveResponse.jsonPath().get("id");
        Integer version = archiveResponse.jsonPath().get("version");
        assertThat(id).isEqualTo(diagramId);
        assertThat(version).isNotNull();
        return new DiagramInfo(id, version);
    }

    private String backfillRequestForDiagram(String diagramId, int version) {
        return """
                message: JmeDiagramVersionCreatedEvent
                archiveDataReferences:
                  - id: %s
                    version: %d
                """.formatted(diagramId, version);
    }

    @Test
    void archiveDiagram() {

        // Data to archive
        final String idToArchive = UUID.randomUUID().toString();
        final String nameToArchive = "test-diagram";
        final String graphToArchive = "test-graph";

        // Archive a Diagram
        Response archiveResponse = given().spec(resourceServiceRequest).contentType(ContentType.JSON)
                .body("{\"name\":\"" + nameToArchive + "\",\"graph\":\"" + graphToArchive + "\"}")
                .when().put("/jme-process-archive-resource-service/api/diagrams/" + idToArchive);

        JsonPath jsonPathEvaluator = archiveResponse.jsonPath();
        String id = jsonPathEvaluator.get("id");
        Integer version = jsonPathEvaluator.get("version");
        String name = jsonPathEvaluator.get("name");
        String graph = jsonPathEvaluator.get("graph");
        assertThat(id).isEqualTo(idToArchive);
        assertThat(version).isNotNull();
        assertThat(name).isEqualTo(nameToArchive);
        assertThat(graph).isEqualTo(graphToArchive);

        // Wait for the archived data to be created
        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .until(() -> areArchivedObjectsPresent(Set.of(idToArchive)));

        // Check the archived data
        List<S3ObjectMetadata> s3Objects = getObjectMetadata();
        checkArchivedDiagram(s3Objects, idToArchive, nameToArchive, graphToArchive);
    }

    @Test
    void archiveDeclarationAsJson() {

        // Create a declaration
        final String declarationId = UUID.randomUUID().toString();
        final String payloadToArchive = "test-payload-" + UUID.randomUUID();

        Response archiveResponse = given().spec(resourceServiceRequest).contentType(ContentType.JSON)
                .body("{\"payload\":\"" + payloadToArchive + "\"}")
                .when().put("/jme-process-archive-resource-service/api/declarations/" + declarationId);

        JsonPath jsonPathEvaluator = archiveResponse.jsonPath();
        String returnedDeclarationId = jsonPathEvaluator.get("declarationId");
        String processId = jsonPathEvaluator.get("processId");
        assertThat(returnedDeclarationId).isEqualTo(declarationId);
        assertThat(processId).isNotNull();

        // The referenceId in S3 is processId + "#" + declarationId
        String expectedReferenceId = processId + "#" + declarationId;

        // Wait for the archived data to be created
        waitAtMost(TIMEOUT)
                .pollInSameThread()
                .pollInterval(POLL_INTERVAL)
                .until(() -> areArchivedObjectsPresent(Set.of(expectedReferenceId)));

        // Check the archived data via the generic payload endpoint
        List<S3ObjectMetadata> s3Objects = getObjectMetadata();
        checkArchivedDeclaration(s3Objects, expectedReferenceId, payloadToArchive);
    }

    private void checkArchivedDeclaration(List<S3ObjectMetadata> s3Objects, String referenceId, String expectedPayload) {
        log.info("Check archived declaration with referenceId {}", referenceId);
        Optional<S3ObjectMetadata> declaration = s3Objects.stream().filter(o -> o.referenceId.equals(referenceId)).findFirst();
        assertThat(declaration).isPresent();

        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/payload/" + getBucketName() + "/" + declaration.get().keyName);
        assertThat(response.getBody().asString()).contains(expectedPayload);
    }

    private List<S3ObjectMetadata> getObjectMetadata() {
        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/" + getBucketName() + "/list");
        List<S3ObjectMetadata> metadataList = Arrays.asList(response.getBody().as(S3ObjectMetadata[].class));
        System.out.println("Size: " + metadataList.size());
        return metadataList;
    }

    boolean areArchivedObjectsPresent(Set<String> archivedObjectIds) {
        return getObjectMetadata().stream()
                .map(o -> o.referenceId)
                .collect(Collectors.toSet())
                .containsAll(archivedObjectIds);
    }

    boolean isArchivedObjectPresent(String referenceId, String schema) {
        return findByReferenceIdAndSchema(getObjectMetadata(), referenceId, schema).isPresent();
    }

    private Optional<S3ObjectMetadata> findByReferenceIdAndSchema(List<S3ObjectMetadata> s3Objects, String referenceId, String schema) {
        return s3Objects.stream()
                .filter(o -> o.referenceId.equals(referenceId) && schema.equals(o.schema))
                .findFirst();
    }

    private void checkArchivedDiagram(List<S3ObjectMetadata> s3Objects, String diagramId, String nameToArchive, String graphToArchive) {
        log.info("Check archived diagram with id {}", diagramId);
        Optional<S3ObjectMetadata> diagram = s3Objects.stream().filter(o -> o.referenceId.equals(diagramId)).findFirst();
        assertThat(diagram).isPresent();

        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/payload/" + getBucketName() + "/" + diagram.get().keyName);
        assertThat(response.getBody().asString())
                .contains(diagramId)
                .contains(nameToArchive)
                .contains(graphToArchive);

        checkRetention(diagram.get().keyName, "COMPLIANCE", ZonedDateTime.now().plusHours(23));
        checkTag(diagram.get().keyName, "JME_Diagram_1_1");
    }

    private void checkArchivedDecree(List<S3ObjectMetadata> s3Objects, String decreeId, String decreeDataToArchive, String titleToArchive) {
        log.info("Check archived decree with id {}", decreeId);
        // The decree id references two artifacts (Decree and DecreeSummary), select by schema
        Optional<S3ObjectMetadata> decree = findByReferenceIdAndSchema(s3Objects, decreeId, "Decree");
        assertThat(decree).isPresent();

        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/payload-string/" + getBucketName() + "/" + decree.get().keyName);
        assertThat(response.getBody().asString())
                .contains(decreeDataToArchive)
                .contains(titleToArchive);

        checkRetention(decree.get().keyName, "COMPLIANCE", ZonedDateTime.now().plusDays(29));
        assertEncrypted(decree.get().keyName);
        checkTag(decree.get().keyName, "JME_Decree_30_30");
    }

    private void checkArchivedDecreeSummary(List<S3ObjectMetadata> s3Objects, String decreeId, String titleToArchive) {
        log.info("Check archived decree summary for decree with id {}", decreeId);
        Optional<S3ObjectMetadata> decreeSummary = findByReferenceIdAndSchema(s3Objects, decreeId, "DecreeSummary");
        assertThat(decreeSummary).isPresent();

        // The decree summary is not encrypted, the payload can be read directly
        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/payload/" + getBucketName() + "/" + decreeSummary.get().keyName);
        assertThat(response.getBody().asString())
                .contains(decreeId)
                .contains(titleToArchive);

        checkRetention(decreeSummary.get().keyName, "COMPLIANCE", ZonedDateTime.now().plusDays(29));
        checkTag(decreeSummary.get().keyName, "JME_DecreeSummary_30_30");
    }

    private void checkVariantOfSharedArchivedArtifactVersionCreatedEventPresent(String id, String variant) {
        log.info("Check SharedArchivedArtifactVersionCreatedEvent for with id {}", id);
        Response response = given().spec(resourceServiceRequest)
                .when().get("/jme-process-archive-resource-service/api/message/" + id);
        assertThat(response.getBody().asString())
                .contains("\"referenceId\":\"" + id + "\"")
                .containsIgnoringCase("\"variant\":\"" + variant + "\"")
                .contains("\"type\":\"SharedArchivedArtifactVersionCreatedEvent\"");
    }

    private boolean isSharedArchivedArtifactVersionCreatedEventPresent(String decreeId) {
        log.info("Check SharedArchivedArtifactVersionCreatedEvent for decree with id {}", decreeId);
        Response response = given().spec(resourceServiceRequest)
                .when().get("/jme-process-archive-resource-service/api/message/messages");
        return response.getBody().asString().contains(decreeId);
    }

    private void checkArchivedDecreeDocument(List<S3ObjectMetadata> s3Objects, String decreeDocumentId, String decreeDataToArchive, String titleToArchive) {
        Optional<S3ObjectMetadata> decreeDocument = s3Objects.stream().filter(o -> o.referenceId.equals(decreeDocumentId)).findFirst();
        assertThat(decreeDocument).isPresent();

        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/payload/" + getBucketName() + "/" + decreeDocument.get().keyName);
        assertThat(response.getBody().asString())
                .contains(decreeDataToArchive)
                .contains(titleToArchive);

        checkRetention(decreeDocument.get().keyName, "COMPLIANCE", ZonedDateTime.now().plusDays(29));
        checkTag(decreeDocument.get().keyName, "JME_DecreeDocument_30_30");
    }

    private void checkArchivedDecreeConverterToSearchItem(List<S3ObjectMetadata> s3Objects, String decreeId, String titleToArchive, String accessToken) {
        Optional<S3ObjectMetadata> decree = findByReferenceIdAndSchema(s3Objects, decreeId, "Decree");
        assertThat(decree).isPresent();

        Response response = given().spec(archiveServiceRequest).auth().oauth2(accessToken)
                .when().get("/process-archive/index-api/searchitems?index_type=JmeDecree&origin_id=" + getBucketName() + ":" + decree.get().keyName);
        assertThat(response.getBody().asString())
                .contains("\"title\":\"" + titleToArchive + "\"");
    }

    private void checkArchivedDecreeDocumentConverterToSearchItem(List<S3ObjectMetadata> s3Objects, String decreeDocumentId, String accessToken) {
        Optional<S3ObjectMetadata> decreeDocument = s3Objects.stream().filter(o -> o.referenceId.equals(decreeDocumentId)).findFirst();
        assertThat(decreeDocument).isPresent();

        Response response = given().spec(archiveServiceRequest).auth().oauth2(accessToken)
                .when().get("/process-archive/index-api/searchitems?index_type=JmeDecreeDocument&origin_id=" + getBucketName() + ":" + decreeDocument.get().keyName);
        assertThat(response.getBody().asString())
                .contains("\"document_id\":\"" + decreeDocumentId + "\"");
    }

    private void checkArchivedDecreeDocumentWithArchiveReader(List<S3ObjectMetadata> s3Objects, String decreeDocumentId) {
        Optional<S3ObjectMetadata> decreeDocument = s3Objects.stream().filter(o -> o.referenceId.equals(decreeDocumentId)).findFirst();
        assertThat(decreeDocument).isPresent();

        Response response = given().spec(inspectionServiceRequest).when().get("/jme-process-archive-inspection-service/api/decree-document/" + getBucketName() + "/" + decreeDocument.get().keyName);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String documentId = jsonPathEvaluator.get("documentId");
        assertThat(documentId).isEqualTo(decreeDocumentId);
    }

    private void checkRetention(String key, String expectedMode, ZonedDateTime expectedRetainUntil) {
        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/retention/" + getBucketName() + "/" + key);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String mode = jsonPathEvaluator.get("mode");
        ZonedDateTime retainUntil = ZonedDateTime.parse(jsonPathEvaluator.get("retainUntil"));
        assertThat(mode).isEqualTo(expectedMode);
        assertThat(retainUntil).isAfter(expectedRetainUntil);
    }

    private void assertEncrypted(String key) {
        Response response = given().spec(inspectionServiceRequest)
                .when().get("/jme-process-archive-inspection-service/api/archived-data/metadata/" + getBucketName() + "/" + key);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String isEncrypted = jsonPathEvaluator.get("is_encrypted");
        assertTrue(Boolean.parseBoolean(isEncrypted));
    }

    private void checkTag(String key, String expectedTag) {
        Response response = given().spec(inspectionServiceRequest).when().get("/jme-process-archive-inspection-service/api/archived-data/tags/" + getBucketName() + "/" + key);
        JsonPath jsonPathEvaluator = response.jsonPath();
        String tag = jsonPathEvaluator.get("ArchiveTypeLifecyclePolicy");
        assertThat(tag).isEqualTo(expectedTag);
    }

    @BeforeEach
    void setUp() {
        String deployStage = System.getProperty(DEPLOY_STAGE_PROPERTY_NAME, "local");
        deployPlatform = System.getProperty(DEPLOY_PLATFORM_PROPERTY_NAME, "local");

        resourceServiceRequest = buildRequestSpec(getBaseUri(deployPlatform, deployStage, 8082));
        inspectionServiceRequest = buildRequestSpec(getBaseUri(deployPlatform, deployStage, 8083));
        archiveServiceRequest = buildRequestSpec(getBaseUri(deployPlatform, deployStage, 8080));

        if ("local".equalsIgnoreCase(deployStage)) {
            authServerUrl = "http://localhost:8081/jme-process-archive-auth-scs/oauth2/token";
        } else {
            if ("aws".equalsIgnoreCase(deployPlatform)) {
                authServerUrl = "https://jme-dev.ingress.nivel.bazg.admin.ch/jme-process-archive-auth-scs/oauth2/token";
            } else {
                authServerUrl = "https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-process-archive-auth-scs/oauth2/token";
            }
        }

        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.config.getLogConfig().blacklistHeader(HttpHeaders.AUTHORIZATION, HttpHeaders.SET_COOKIE);
        RestAssured.filters(new ResponseLoggingFilter());
    }

    private String retrieveAccessToken() {
        // A client must be defined with the role "jme_@searchitem_#read" in the OAuth-Mock-Server on DEV
        String clientSecret = "secret";
        return RestAssured.given()
                .config(RestAssured.config().encoderConfig(encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", "jme-process-archive-it-client")
                .formParam("client_secret", clientSecret)
                .post(this.authServerUrl)
                .jsonPath().get("access_token");
    }

    private static String getBaseUri(String platform, String stage, int localPort) {
        return switch (platform) {
            case "local" -> "http://localhost:" + localPort;
            case "aws" -> "https://jme-%s.ingress.nivel.bazg.admin.ch".formatted(stage);
            case "rhos" -> "https://bit-jme-%s.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch".formatted(stage.charAt(0));
            default ->
                    throw new IllegalArgumentException("Unknown value for property " + DEPLOY_PLATFORM_PROPERTY_NAME);
        };
    }

    private static RequestSpecification buildRequestSpec(String baseUri) {
        return new RequestSpecBuilder().setBaseUri(baseUri).build();
    }

    private String getBucketName() {
        return switch (deployPlatform) {
            case "local" -> "bit-jme-processarchive-lockable-obs-local";
            case "aws" -> "jme-processarchive-lockable-obs-nivel-dev";
            case "rhos" -> "bit-jme-processarchive-lockable-obs";
            default ->
                    throw new IllegalArgumentException("Unknown value for property " + DEPLOY_PLATFORM_PROPERTY_NAME);
        };
    }
}
