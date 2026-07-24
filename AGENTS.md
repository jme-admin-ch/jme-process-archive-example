# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Example project demonstrating the **jEAP Process Archive Service (PAS)**: a resource service publishes domain events/commands over Kafka, a PAS instance consumes them and archives artifacts into an S3-compatible object store (with object-lock, encryption via Vault, and optional OpenSearch indexing). Multi-module Maven project, parent `jeap-spring-boot-parent`. The README (German) is the authoritative end-to-end walkthrough including curl examples for local and DEV environments.

All jEAP/BIT dependencies come from internal repositories (`repo.bit.admin.ch`); builds only work with access to them. CI is Jenkins (`jeapBuildPipeline`, see `Jenkinsfile`).

## Commands

```bash
./mvnw clean install                       # build everything
./mvnw -pl jme-process-archive-service test                         # tests for one module
./mvnw -pl jme-process-archive-service test -Dtest=DecreeCreatedDataProviderTest   # single test class
```

Local runtime environment (Kafka + schema registry, PostgreSQL, RustFS S3, Vault):

```bash
cd docker && docker compose up            # docker compose down -v to reset stale volumes
```

Run services (from project root; IntelliJ run configs also exist in `.run/`):

```bash
./mvnw -pl jme-process-archive-auth-scs spring-boot:run -Dspring-boot.run.profiles=local        # :8081
./mvnw -pl jme-process-archive-resource-service spring-boot:run -Dspring-boot.run.profiles=local # :8082
./mvnw -pl jme-process-archive-service spring-boot:run -Dspring-boot.run.profiles=local          # :8080, context /process-archive
./mvnw -pl jme-process-archive-inspection-service spring-boot:run -Dspring-boot.run.profiles=local # :8083
```

Spring profiles per environment: `local`, `aws`, `rhos` (resource service also has `cloud`), each with its own `application-<profile>.yml`.

## Architecture

Modules:

- **`jme-process-archive-events`** — thin builder classes (`*EventBuilder`) around the Avro message types. The Avro-generated events/commands themselves are external Maven dependencies from the message-type registry (`ch.admin.bit.jme.messagetype.jme:*`).
- **`jme-process-archive-resource-service`** — owns the example domain (Decree, DecreeDocument, Diagram, Declaration). REST controllers under `/api/*` create entities and publish the corresponding events/commands to Kafka. Also exposes archival-fetch endpoints (`/api/archive/decreedocuments/{id}`, `/api/diagrams/{id}/archival/...`) that the PAS calls back for event-notification-style archiving.
- **`jme-process-archive-service`** — the PAS instance. **Has no own `Application` class**: the main class is `ch.admin.bit.jeap.processarchive.service.ProcessArchiveApplication` from the jEAP library (configured in the pom for spring-boot/jib plugins). This module only contributes plugin implementations plus configuration.
- **`jme-process-archive-inspection-service`** — REST access to archived objects in S3 (`/api/archived-data/...`: payload, decrypted payload-string, metadata, retention, tags, lifecycle) and a `DecreeDocument` reader using the Process Archive Reader library.
- **`jme-process-archive-auth-scs`** — OAuth mock server (`jeap-oauth-mock-server`), no own Java code. Issues tokens for the protected PAS APIs (backfill jobs, SearchItems API).

### How the PAS module is wired

`src/main/resources/processarchive/messages.json` is the central config: it maps consumed message types + topics to provider/condition classes **by fully qualified class name**. When adding or renaming a provider/condition, update this file. The same message/topic may appear **multiple times** — one entry (provider/condition/featureFlag) per artifact to archive; e.g. `JmeDecreeCreatedEvent` is registered twice (`DecreeCreatedDataProvider` → `Decree` artifact, `DecreeSummaryDataProvider` → `DecreeSummary` artifact). When a message has multiple entries, each entry must define a unique non-blank `id` (startup fails otherwise). Artifacts of the same message must differ in system/schema/referenceId/version, since those form the idempotence-ID discriminator (duplicates fail fast at runtime). The two archiving styles demonstrated:

- **Event Carried State Transfer** (payload in the message): implement `MessageArchiveDataProvider`/`DomainEventArchiveDataProvider` (e.g. `DecreeCreatedDataProvider`, `CreateDeclarationCommandDataProvider` — the latter archives JSON instead of Avro) plus a `MessageCorrelationProvider`.
- **Event Notification** (message carries only a reference): implement `ArchiveDataReferenceProvider` (e.g. `DecreeDocumentCreatedArchiveDataReferenceProvider` — unversioned, id only; `DiagramVersionCreatedArchiveDataReferenceProvider` — versioned, id + version). The PAS then fetches the artifact from the REST `uri` configured in `messages.json`.

Other extension points in this module: `condition/` (`ArchiveCondition` implementations to skip archiving), `indextype/` (converters producing OpenSearch SearchItems, wired via `processarchive/indextypes.json`), `JmeArchiveTypeProvider` (registers archive types pulled in as `ch.admin.bit.jme.archivetype.jme:*` deps; one archive type, `JsonExample`, is registered via `application.yml` instead). New Avro archive types are declared in the sibling repo `../jme-archive-type-registry` (descriptor JSON + `.avdl` per version under `archive-types/jme/`) and published as Maven artifacts from there — never invent archive-type artifacts locally. `ProcessArchiveMessageContracts` declares the `@JeapMessageConsumerContract`/`@JeapMessageProducerContract` annotations required for runtime contract checking — new consumed/produced message types must be added there and in the message-type registry.

After archiving, the PAS publishes a `SharedArchivedArtifactVersionCreatedEvent` per artifact (built into the jEAP library, configured under `jeap.processarchive.archivedartifact` in `application.yml`); no custom `ArtifactArchivedListener` is registered in this example. The required `HashProvider` bean (the jEAP library has no default) is provided by `JmeHashProvider` (BLAKE3 via Bouncy Castle), registered via the module's `AutoConfiguration.imports`.

### Backfill

Optional mechanism to archive artifacts retroactively without a domain event: PUT a YAML job to
`/process-archive/api/jobs/{jobId}` (OAuth-protected, idempotent per job id), the PAS persists job/tasks in PostgreSQL
and publishes a `CreateArtifactCommand` per reference via the outbox; report via GET `.../jobs/{jobId}/report`. Enabled
with `jeap.processarchive.backfill.enabled: true` + backfill topic; requires the
`jeap-process-archive-adapter-rest-api`/`-adapter-db` dependencies and the PostgreSQL setup. A backfill job targets
exactly one uri-based (remote-data) configuration, resolved from `message` plus optional `config-id` (the entry's `id`
in `messages.json`) — jobs carry no topic; if a message has several remote-data configurations, `config-id` is
mandatory, otherwise the job is rejected as ambiguous. The README documents the full local test flow.

### Versions

jEAP library versions (`jeap-process-archive-service.version`, `jeap-process-archive-reader.version`, etc.) are properties in the root `pom.xml`.
