# Process Archive Examples

This document describes the features demonstrated by the JME Process Archive Example and provides commands for trying
them locally.

> Make sure the Docker Compose infrastructure and all four services described in
> [README.md](./README.md) are running before executing the examples.

## Example artifacts and messages

The resource service owns four example artifact types:

- **Decree:** An Avro record carried directly by `JmeDecreeCreatedEvent`.
- **DecreeDocument:** An unversioned Avro document referenced by `JmeDecreeDocumentCreatedEvent`.
- **Diagram:** A versioned Avro artifact referenced by `JmeDiagramVersionCreatedEvent`.
- **Declaration:** A JSON document carried by `JmeCreateDeclarationCommand`.

Creating a decree also creates a decree document. The resource service publishes the corresponding messages, and the
Process Archive Service consumes them and archives the artifacts.

### Event-carried state transfer

`DecreeCreatedDataProvider` extracts a decree directly from `JmeDecreeCreatedEvent`.
`CreateDeclarationCommandDataProvider` extracts a declaration from `JmeCreateDeclarationCommand` and archives it as
`application/json` instead of Avro. The associated correlation providers extract the process ID from each message.

### Event notification

`DecreeDocumentCreatedArchiveDataReferenceProvider` extracts an unversioned decree-document ID from
`JmeDecreeDocumentCreatedEvent`. The Process Archive Service then retrieves the document from the resource service
endpoint configured in `processarchive/messages.json`.

`DiagramVersionCreatedArchiveDataReferenceProvider` extracts both the diagram ID and its version from
`JmeDiagramVersionCreatedEvent`. A configured archive condition skips diagrams whose ID contains `ignored`.

## Multiple artifacts from one message

The example registers two configurations for `JmeDecreeCreatedEvent`:

- `DecreeCreatedDataProvider` archives the encrypted `Decree` artifact.
- `DecreeSummaryDataProvider` archives an unencrypted `DecreeSummary` artifact.

Each configuration in `processarchive/messages.json` has a unique, non-empty `id`. This is required whenever the same
message and topic are registered more than once and also allows a configuration to be addressed by a backfill job.

Artifacts created from the same message must differ in system, schema, reference ID, or version. These values form part
of the archive idempotence ID. The two decree artifacts share a reference ID but use different schemas.

Feature flags apply independently to each configuration. They control publication of
`SharedArchivedArtifactVersionCreatedEvent`; they do not prevent the object itself from being stored.

## Process Archive Service configuration

The central configuration is
`jme-process-archive-service/src/main/resources/processarchive/messages.json`. It maps consumed message types and
topics to archive data providers, reference providers, conditions, correlation providers, and feature flags.

The example also demonstrates:

- `ProcessArchiveMessageContracts`, which declares the producer and consumer contracts required for runtime checks.
- `JmeHashProvider`, which provides BLAKE3 hashes for archived artifact metadata.
- `JmeArchiveTypeProvider`, which registers the example archive types.
- `processarchive/indextypes.json`, which configures converters for OpenSearch search items.

After successful archival, the service publishes one `SharedArchivedArtifactVersionCreatedEvent` per artifact. The
example does not register a custom `ArtifactArchivedListener`.

## Create artifacts

### Decree and decree document

```shell
curl --header "Content-Type: application/json" --request POST \
  --data '{"title":"test-title","someDecreeData":"test-data"}' \
  http://localhost:8082/jme-process-archive-resource-service/api/decrees
```

This request results in three archived objects: `Decree`, `DecreeSummary`, and `DecreeDocument`.

### Diagram version

```shell
curl --header "Content-Type: application/json" --request PUT \
  --data '{"name":"test-diagram","graph":"test-graph"}' \
  http://localhost:8082/jme-process-archive-resource-service/api/diagrams/example
```

Repeating the request for the same ID creates another diagram version.

### JSON declaration

```shell
curl --header "Content-Type: application/json" --request PUT \
  --data '{"payload":"test-payload"}' \
  http://localhost:8082/jme-process-archive-resource-service/api/declarations/example
```

## Backfill

Backfill archives existing remote artifacts without requiring the original domain event. A job targets exactly one
URI-based configuration. It is resolved by `message` and, when necessary, `config-id`.

First, create a decree document and capture its ID:

```shell
DECREE_DOCUMENT_ID=$(curl -s --header "Content-Type: application/json" --request POST \
  --data '{"title":"test-title","someDecreeData":"test-data"}' \
  http://localhost:8082/jme-process-archive-resource-service/api/decrees \
  | jq -r .decreeDocumentId)
```

Obtain a token from the local OAuth mock server:

```shell
TOKEN=$(curl -s --request POST \
  http://localhost:8081/jme-process-archive-auth-scs/oauth2/token \
  --header "Content-Type: application/x-www-form-urlencoded" \
  --data "grant_type=client_credentials&client_id=jme-process-archive-it-client&client_secret=secret" \
  | jq -r .access_token)
```

Create a job file for the unversioned decree document:

```yaml
message: JmeDecreeDocumentCreatedEvent
archiveDataReferences:
  - id: 81572315-ab4d-482e-8254-7b049961f46b
```

Versioned artifacts include a version:

```yaml
message: JmeDiagramVersionCreatedEvent
archiveDataReferences:
  - id: example
    version: 2
```

If a message has multiple remote-data configurations, select one by its ID from `messages.json`:

```yaml
message: JmeDecreeDocumentCreatedEvent
config-id: decree-document
archiveDataReferences:
  - id: 81572315-ab4d-482e-8254-7b049961f46b
```

Submit the YAML file:

```shell
JOB_ID=$(uuidgen)

curl -i --request PUT "http://localhost:8080/process-archive/api/jobs/${JOB_ID}" \
  --header "Authorization: Bearer ${TOKEN}" \
  --header "Content-Type: application/yaml" \
  --data-binary @pas-backfill-job.yaml
```

Submitting identical content again with the same job ID is idempotent. Retrieve the report with:

```shell
curl --request GET "http://localhost:8080/process-archive/api/jobs/${JOB_ID}/report" \
  --header "Authorization: Bearer ${TOKEN}" \
  --header "Accept: application/yaml"
```

The Process Archive Service stores jobs and tasks in PostgreSQL and publishes a `CreateArtifactCommand` through the
outbox for each reference.

## Inspect archived objects

The inspection service exposes the following endpoints for the local bucket:

| Information | Endpoint |
| --- | --- |
| Payload | `/api/archived-data/payload/{bucket}/{key}` |
| Decrypted payload as text | `/api/archived-data/payload-string/{bucket}/{key}` |
| Metadata | `/api/archived-data/metadata/{bucket}/{key}` |
| Retention | `/api/archived-data/retention/{bucket}/{key}` |
| Tags | `/api/archived-data/tags/{bucket}/{key}` |
| Bucket lifecycle | `/api/archived-data/{bucket}/lifecycle/configuration` |
| Objects stored today | `/api/archived-data/{bucket}/list` |

Use the base URL
`http://localhost:8083/jme-process-archive-inspection-service` and the bucket
`bit-jme-processarchive-lockable-obs-local`.

The object key and version ID are available in the Process Archive Service log and in
`SharedArchivedArtifactVersionCreatedEvent`. Add `?version={objectVersionId}` to retrieve a specific object version.

The `/api/decree-document` endpoint demonstrates the Process Archive Reader library by reading and returning an archived
`DecreeDocument`.

## SearchItems API

The Process Archive Service exposes the protected `/index-api/searchitems` endpoint for creating OpenSearch search items
from archived artifacts. Requests can filter by:

- `index_type`: The search-item type and therefore the fields included in the result.
- `origin_id`: The bucket ID and object key of the archived artifact.
- `origin_version`: An optional object version; without it, the latest version is used.

The local Swagger UI is available at:

`http://localhost:8080/process-archive/swagger-ui/index.html?urls.primaryName=OpenSearch+SearchItems+API`

Use a JWT issued by the local OAuth mock server when calling this API.
