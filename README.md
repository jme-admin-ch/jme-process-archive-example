# JME Process Archive Example

This project demonstrates how a microservice can use an instance of the
[jEAP Process Archive Service](https://github.com/jeap-admin-ch/jeap-process-archive-service) to archive artifacts
automatically when domain events or commands are received.

It contains the following modules:

- `jme-process-archive-events`: Builders for the example messages.
- `jme-process-archive-resource-service`: A sample domain service that creates artifacts and publishes messages.
- `jme-process-archive-service`: An example Process Archive Service instance.
- `jme-process-archive-inspection-service`: REST endpoints for inspecting archived objects.
- `jme-process-archive-auth-scs`: An OAuth mock server for protected Process Archive Service APIs.
- `jme-process-archive-test`: End-to-end integration tests.

The example covers event-carried state transfer, event notification, versioned and unversioned artifacts, JSON and
Avro payloads, encryption, multiple artifacts per event, archive conditions, backfill, and archive inspection. See
[ARCHIVE-EXAMPLES.md](./docs/ARCHIVE-EXAMPLES.md) for the detailed feature descriptions and step-by-step examples.

## Changes

This project is versioned using [Semantic Versioning](https://semver.org/), and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined by [Keep a Changelog](https://keepachangelog.com/).

## Prerequisites

To build and run the project locally, ensure that the following are installed:

1. Java Development Kit (JDK) 25.
2. Docker with Docker Compose.

Use the provided Maven wrapper to build and run the project.

## Getting started

### Infrastructure

Start Kafka, the schema registry, PostgreSQL, the S3-compatible RustFS object store, and Vault:

```shell
docker compose -f docker/docker-compose.yml up
```

RustFS is available locally at:

- S3 API: `http://localhost:9101`
- Console: `http://localhost:9001` using `rustfsadmin` / `rustfsadmin`

The `bit-jme-processarchive-lockable-obs-local` bucket is created automatically with object lock enabled.

### Build

```shell
./mvnw install
```

### Start

Start the services in separate terminals:

```shell
./mvnw -pl jme-process-archive-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-process-archive-resource-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-process-archive-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl jme-process-archive-inspection-service spring-boot:run -Dspring-boot.run.profiles=local
```

Equivalent IntelliJ run configurations are included in the repository.

If an incompatible PostgreSQL volume exists from an earlier run, reset the local infrastructure:

```shell
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up
```

## Try the examples

The quickest way to create artifacts is:

```shell
curl --header "Content-Type: application/json" --request POST \
  --data '{"title":"test-title","someDecreeData":"test-data"}' \
  http://localhost:8082/jme-process-archive-resource-service/api/decrees

curl --header "Content-Type: application/json" --request PUT \
  --data '{"name":"test-diagram","graph":"test-graph"}' \
  http://localhost:8082/jme-process-archive-resource-service/api/diagrams/example

curl --header "Content-Type: application/json" --request PUT \
  --data '{"payload":"test-payload"}' \
  http://localhost:8082/jme-process-archive-resource-service/api/declarations/example
```

The Process Archive Service consumes the resulting messages and archives the corresponding artifacts. For the complete
walkthrough, including backfill and inspection endpoints, see
[ARCHIVE-EXAMPLES.md](./docs/ARCHIVE-EXAMPLES.md).

## Profiles

- `local`: Local development using the Docker Compose infrastructure.
- `aws`: Deployment using AWS-specific infrastructure.
- `rhos`: Deployment on Red Hat OpenShift.

## Note

This repository is part of the open source distribution of JME. See
[github.com/jme-admin-ch/jme](https://github.com/jme-admin-ch/jme) for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
