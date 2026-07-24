# JME Process Archive Example

## Übersicht

Dieses Beispiel zeigt, wie ein Microservice (``jme-process-archive-resource-service``) Artefakte mit Hilfe einer Instanz
(``jme-process-archive-service``) des Process-Archive-Service automatisch in einem Object-Store aufgrund bestimmter
Domain-Events
archivieren lassen kann.

## Voraussetzung

Das ``docker-compose.yml`` im ``docker``-Verzeichnis dieses Beispiels erzeugt die vom Beispiel benötigte
Laufzeitumgebung mit einem
Kafka (Broker, Schema-Registry), einer PostgreSQL-Datenbank und einem S3-kompatiblen Object-Store (RustFS).

Im Beispiel wird das Decree verschlüsselt im S3-Object-Store abgelegt. Deswegen ist eine Vault-Instanz im
``docker-compose.yml``
auch definiert und konfiguriert.

> cd docker && docker compose up

RustFS ist lokal unter folgenden URLs erreichbar:

* S3 API: ``http://localhost:9101``
* Console: ``http://localhost:9001`` mit ``rustfsadmin`` / ``rustfsadmin``

Der Bucket ``bit-jme-processarchive-lockable-obs-local`` wird beim Starten von Docker Compose automatisch mit
Object-Lock
erstellt.

Das Aufstarten der Docker-Container ist nur lokal notwendig. In der DEV-Umgebung verwendet das Beispiel die
entsprechenden
DEV-Kafka-, -StorageGRID- und -Vault-Instanzen.

## Resource-Service

Der ``jme-process-archive-resource-service`` gibt ein Beispiel für eine Resource von zu archivierenden Artefakten:

* Decree: Eine Verfügung, die ausgestellt werden kann. Modelliert die Verfügung als Datensatz im Avro-Format.
* DecreeDocument: Ein auf der Basis eines Verfügungsdatensatze erstelltes Verfügungsdokument im Avro-Format.
* Diagram: Ein Diagramm, welches versioniert verwaltet wird im Avro-Format.
* Declaration: Eine Deklaration, die als JSON-Datensatz (nicht Avro) archiviert wird.

Der Resource-Service teilt der Aussenwelt das Erstellen einer Verfügung, das Erstellen des Verfügungsdokuments, das
Erstellen einer neuen Diagramm-Version und das Erstellen einer Deklaration über Events bzw. Commands mit:

* ``JmeDecreeCreatedEvent``, eine Verfügung wurde erstellt.
* ``JmeDecreeDocumentCreatedEvent``, ein Verfügungsdokument wurde erstellt.
* ``JmeDiagramVersionCreatedEvent``, eine neue Diagramm-Version wurde erstellt.
* ``JmeCreateDeclarationCommand``, eine Deklaration wird erstellt.

Zur Ausstellung einer neuen Verfügung kann ein POST-Request auf den ``/api/decrees``-Endpunkt mit einer JSON-Payload
abgesetzt werden:

    {
        "title" : "decree title",
        "someDecreeData": "decree data"
    }

Der Post-Request wir eine neue Verfügung und das daraus abgeleiteten Verfügungsdokument erzeugen.

Zur Ausstellung einer neuen Diagramm-Version kann ein PUT-Request auf den ``/api/diagrams/{id}``-Endpunkt mit einer
JSON-Payload abgesetzt werden:

    {
        "name" : "diagram name",
        "graph": "diagram content definition"
    }

Der PUT-Request wird eine neue Version des Diagramms mit der im URL-Parameter definierten Id und den im JSON-Payload
spezifizierten Daten erzeugen.

Zur Erstellung einer neuen Deklaration kann ein PUT-Request auf den ``/api/declarations/{declarationId}``-Endpunkt mit
einer
JSON-Payload abgesetzt werden:

    {
        "payload" : "declaration payload text"
    }

Der PUT-Request wird eine neue Deklaration mit der im URL-Parameter definierten Id und dem im JSON-Payload
spezifizierten Inhalt erzeugen.

Der Resource-Service stellt unter ```/api/archive/decreedocuments/{id}``` einen Endpunkt zur Verfügung, über den
Verfügungsdokumente zur Archivierung bezogen werden können. Spezifische Diagramm-Versionen können dagegen unter dem
Endpunkt
```/api/diagrams/{id}/archival/version={version}``` zur Archivierung bezogen werden.

## Process-Archive-Service

Der ``jme-process-archive-service`` gibt ein Beispiel für die Instanzierung eine jEAP-Process-Archive-Services. Er hört
auf die Events ``JmeDecreeCreatedEvent``, ``JmeDecreeDocumentCreatedEvent``, ``JmeDiagramVersionCreatedEvent`` sowie
den Command ``JmeCreateDeclarationCommand`` und archiviert die zugehörigen Archivdaten.

Die Events und Commands sind von unterschiedlicher Natur:

* ``JmeDecreeCreatedEvent`` entspricht dem Ansatz "Event Carried State Transfer" und enthält alle Daten einer Verfügung.
* ``JmeDecreeDocumentCreatedEvent`` und ``JmeDiagramVersionCreatedEvent`` entsprechen dem Ansatz "Event Notification"
  und enthalten je nur die Referenz auf das erstellte Verfügungsdokument bzw. auf die spezifische Version eines
  Diagramms.
* ``JmeCreateDeclarationCommand`` entspricht dem Ansatz "Event Carried State Transfer" und enthält die Daten einer
  Deklaration.
  Im Gegensatz zu den anderen Beispielen wird die Deklaration nicht im Avro-Format, sondern als JSON archiviert.

Entsprechend erfolgt auch die Archivierung einer Verfügung, eines Verfügungsdokuments und eines Diagramms auf
unterschiedliche Art und Weise:

* Für die Archivierung einer Verfügung implementiert ``DecreeCreatedDataProvider`` einen spezifischen
  ``MessageArchiveDataProvider``,
  welcher die zu archivierenden Daten einer Verfügung direkt aus dem Domain-Event ``JmeDecreeCreatedEvent`` extrahiert.
  Für das gleiche Event ist zusätzlich der ``DecreeSummaryDataProvider`` registriert, welcher aus demselben Domain-Event
  ein zweites Artefakt (eine Zusammenfassung der Verfügung vom Archive-Type ``DecreeSummary``) extrahiert und archiviert
  (siehe [Mehrere Artefakte pro Domain-Event](#mehrere-artefakte-pro-domain-event)).
* Für die Archivierung eines Verfügungsdokuments implementiert ``DecreeDocumentCreatedArchiveDataReferenceProvider``
  einen
  spezifischen ``ArchiveDataReferenceProvider``, welcher aus dem Domain-Event ``JmeDecreeDocumentCreatedEvent`` die
  Referenz auf das
  zu archivierende Verfügungsdokument extrahiert. Von einem Verfügungsdokument kann es nur eine Version geben, d.h.
  Verfügungsdokumente werden nicht versioniert verwaltet. Deshalb besteht die extrahierte Referenz in diesem Fall nur
  aus einer Verfügungsdokument-Id. Der Process-Archive-Service wird das Verfügungsdokument dann unter Angabe dieser Id
  von
  demjenigen REST-Endpunkt beziehen, welcher für das ``JmeDecreeDocumentCreatedEvent`` konfiguriert wurde.
* Für die Archivierung einer Diagramm-Version implementiert ``DiagramVersionCreatedArchiveDataReferenceProvider`` einen
  spezifischen ``ArchiveDataReferenceProvider``, welcher aus dem Domain-Event ``JmeDiagramVersionCreatedEvent`` die
  Referenz auf das
  zu archivierende Diagramm extrahiert. Da Diagramme versioniert verwaltet werden, besteht die Referenz in diesem Fall
  aus einer Diagramm-Id und einer Diagramm-Version. Der Process-Archive-Service wird die spezifische Diagramm-Version
  dann
  unter Angabe der extrahierten Id und der extrahierten Version von demjenigen REST-Endpunkt beziehen, welcher für das
  ``JmeDiagramVersionCreatedEvent`` konfiguriert wurde. Für dieses Event ist ausserdem eine Condition konfiguriert,
  Daten zu einem Diagram werden nur archiviert falls die ID nicht den String "ignored" enthält.
* Für die Archivierung einer Deklaration implementiert ``CreateDeclarationCommandDataProvider`` einen spezifischen
  ``MessageArchiveDataProvider``, welcher die zu archivierenden Daten einer Deklaration direkt aus dem Command
  ``JmeCreateDeclarationCommand`` extrahiert. Die Daten werden als JSON (``application/json``) anstatt im Avro-Format
  archiviert. Der zugehörige ``CreateDeclarationCommandReferenceProvider`` implementiert einen
  ``MessageCorrelationProvider``,
  welcher die Prozess-Id aus dem Command extrahiert.

Die Konfiguration, wie der Process-Archive-Service auf Domain-Events reagieren soll, wird in der Ressource
``processarchive/messages.json``
vorgegeben.

#### Mehrere Artefakte pro Domain-Event

Ab Version 16.0.0 des jEAP-Process-Archive-Service können für dieselbe Message (gleicher ``messageName`` und
``topicName``) mehrere Archiv-Konfigurationen in ``processarchive/messages.json`` registriert werden. Jeder Eintrag hat
seinen eigenen Provider (``messageArchiveDataProvider`` oder ``archiveDataReferenceProvider``), seine eigene Condition,
seinen eigenen Correlation-Provider und sein eigenes Feature-Flag — und führt zur Archivierung je eines Artefakts.
Sobald für eine Message mehrere Konfigurationen registriert sind, muss jeder Eintrag eine eindeutige, nicht-leere
``id`` definieren, über welche die Konfiguration eindeutig adressiert werden kann (z.B. für Backfill-Jobs).

Das Beispiel demonstriert dies mit dem ``JmeDecreeCreatedEvent``: Das Event führt zur Archivierung von **zwei**
Artefakten:

* ``DecreeCreatedDataProvider`` archiviert die Verfügung als Artefakt vom Archive-Type ``Decree`` (verschlüsselt).
* ``DecreeSummaryDataProvider`` archiviert eine Zusammenfassung der Verfügung als Artefakt vom Archive-Type
  ``DecreeSummary`` (unverschlüsselt).

Die zugehörige Konfiguration in ``processarchive/messages.json``:

    {
      "id": "decree",
      "messageName": "JmeDecreeCreatedEvent",
      "topicName": "jme-process-archive-decreecreated",
      "messageArchiveDataProvider": "ch.admin.bit.jeap.jme.processarchive.service.provider.DecreeCreatedDataProvider",
      "correlationProvider": "ch.admin.bit.jeap.jme.processarchive.service.provider.DecreeCorrelationProvider",
      "featureFlag": "FEATURE_DECREE_CREATED"
    },
    {
      "id": "decree-summary",
      "messageName": "JmeDecreeCreatedEvent",
      "topicName": "jme-process-archive-decreecreated",
      "messageArchiveDataProvider": "ch.admin.bit.jeap.jme.processarchive.service.provider.DecreeSummaryDataProvider",
      "correlationProvider": "ch.admin.bit.jeap.jme.processarchive.service.provider.DecreeCorrelationProvider",
      "featureFlag": "FEATURE_DECREE_SUMMARY"
    }

Dabei gilt:

* Die Idempotence-Id eines archivierten Artefakts hat das Format
  ``<MessageType>_<MessageIdempotenceId>_<System>_<Schema>_<ReferenceId>[_<Version>]``. Konfigurationen, deren Artefakte
  zur gleichen Idempotence-Id führen würden, werden zur Laufzeit mit einem Fehler abgewiesen ("fail fast"). Die
  Artefakte derselben Message müssen sich also in System, Schema, Reference-Id oder Version unterscheiden. Im Beispiel
  teilen sich die beiden Artefakte die Reference-Id (die Verfügungs-Id), unterscheiden sich aber im Schema
  (``Decree`` bzw. ``DecreeSummary``).
* Die Feature-Flags werden pro Eintrag ausgewertet: Mit ``FEATURE_DECREE_CREATED`` und ``FEATURE_DECREE_SUMMARY`` kann
  die Publikation des ``SharedArchivedArtifactVersionCreatedEvent`` für jedes der beiden Artefakte unabhängig ein- und
  ausgeschaltet werden. Die Ablage im Object-Store selbst wird durch das Feature-Flag nicht unterbunden.
* Ein Backfill-Job bezieht sich immer auf genau eine Remote-Data-Konfiguration (Eintrag mit ``uri``) und wird allein
  über die ``message`` (und optional ``config-id``) aufgelöst. Existieren für eine Message mehrere
  Remote-Data-Konfigurationen, muss der Backfill-Job die gewünschte Konfiguration über das
  Feld ``config-id`` (die ``id`` des Eintrags in ``messages.json``) adressieren; ohne ``config-id`` ist der Backfill
  in diesem Fall mehrdeutig und wird abgewiesen. Bei genau einer Remote-Data-Konfiguration kann ``config-id``
  weggelassen werden. Einträge mit ``messageArchiveDataProvider`` (Inline-Daten) sind davon nicht betroffen.

#### Domain-Event-Contracts

Damit der Process-Archive-Service Domain-Events empfangen kann, muss er in der Message-Type-Library in den Deklarationen
der betroffenen Domain-Event als Empfänger aufgeführt sein. Zudem müssen die benötigten Annotationen in der Application
konfiguriert werden (JeapMessageProducerContracts), damit die entsprechenden Informationen zur Laufzeit des
Process-Archive-Services für eine Contract-Prüfung zur Verfügung stehen.

#### Archived Artifact Event

Nach der Archivierung eines Artefakts publiziert der Process-Archive-Service ein
``SharedArchivedArtifactVersionCreatedEvent`` mit allen Informationen rund um die erfolgte Archivierung
(u.a. die Koordinaten des archivierten Artefakts im Object-Store). Zusätzlich können optional
``ArtifactArchivedListener``-Implementierungen als Spring-Beans registriert werden, die über jedes archivierte
Artefakt benachrichtigt werden; dieses Beispiel registriert keinen eigenen Listener.

#### Hash Provider

Der Process-Archive-Service erzeugt für ein Artefakt einen Hash und speichert diesen in den Meta-Daten des archivierten
Artefakts.
Das zu verwendende Hash-Verfahren muss dem Process-Archive-Service als konkrete ``HashProvider``-Implementierung
(Spring-Bean) zur Verfügung gestellt werden. Im Beispiel geschieht dies durch den ``JmeHashProvider`` (BLAKE3), der
via Auto-Configuration registriert wird.

#### Backfill-Setup

Der Backfill-Mechanismus erlaubt es, Artefakte nachträglich zu archivieren, die ursprünglich nicht über ein Domain-Event
archiviert wurden. Er benötigt eine PostgreSQL-Datenbank (für Job- und Task-Speicherung) sowie zusätzliche
Maven-Abhängigkeiten.

**Maven-Abhängigkeiten** (in ``pom.xml`` des Process-Archive-Service):

```xml
<!-- REST API für den Backfill-Job-Endpunkt -->
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-process-archive-adapter-rest-api</artifactId>
    <version>${jeap-process-archive-service.version}</version>
</dependency>
<!-- Datenbankadapter für Job/Task-Persistierung -->
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-process-archive-adapter-db</artifactId>
    <version>${jeap-process-archive-service.version}</version>
</dependency>
<!-- Flyway-Datenbankmigrationen -->
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-spring-boot-db-migration-starter</artifactId>
</dependency>
<!-- PostgreSQL auf AWS -->
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-spring-boot-postgresql-aws-starter</artifactId>
</dependency>
<!-- PostgreSQL JDBC-Treiber -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Konfiguration in ``application.yml``**:

```yaml
jeap:
  processarchive:
    backfill:
      enabled: true
      topic: jme-process-archive-createartifact  # Kafka-Topic für CreateArtifactCommand

spring:
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    open-in-view: false
    properties:
      hibernate.default_schema: data
  datasource:
    hikari:
      schema: ${spring.jpa.properties.hibernate.default_schema}
      maximum-pool-size: 5
      pool-name: "hikari-cp-${spring.application.name}"
  flyway:
    default-schema: ${spring.jpa.properties.hibernate.default_schema}
```

**Lokale Konfiguration (``application-local.yml``)**:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jme_process_archive
    username: jme_process_archive
    password: jme_process_archive
    hikari:
      schema: data
  jpa:
    properties:
      hibernate.default_schema: data
```

**AWS-Konfiguration (``application-aws.yml``)**:

```yaml
jeap:
  postgresql:
    aws:
      enabled: true
  datasource:
    aws:
      database-name: jme_process_archive_db
```

**RHOS-Konfiguration (``application-rhos.yml``)**:

```yaml
spring:
  datasource:
    hikari:
      schema: data
  jpa:
    properties:
      hibernate:
        default_schema: data
  flyway:
    default-schema: data

database-migration:
  init-container: ${IS_INIT_CONTAINER_EXECUTION}
```

## Inspection-Service

Der ``jme-process-archive-inspection-service`` stellt einen simplen Rest-Controller unter `/api/archived-data` zur
Verfügung, über welchen die im
Object-Store archivierten Daten eingesehen werden können. Er bietet mehrere Endpunkte an:

* ``payload`` erlaubt den Zugriff auf ein Archiv-Objekt
* ``payload-string`` erlaubt den Zugriff auf ein verschlüsseltes Archiv-Objekt, das entschlüsselt wird
* ``metadata`` erlaubt den Zugriff auf die Metadaten eines Archiv-Objekts
* ``retention`` erlaubt den Zugriff auf die Retention-Eigenschaften eines Archiv-Objekts (Object-Version-Locking)
* ``tags`` erlaubt den Zugriff auf die S3-Tags eines Archiv-Objekts
* ``lifecycle/configuration`` erlaubt den Zugriff auf die S3-Lifecycle-Konfiguration eines Buckets

Unter `/api/decree-document` kann ein `DecreeDocument` abgeholt werden. Diese API verwendet die Process Archive Reader
Library und gibt das Objekt zurück.

## Umgebungen

Das Beispiel kann lokal und auf der DEV-Umgebung ausgeführt werden.

#### Lokale Ausführung

1) ``docker-compose.yml`` im ``docker``-Verzeichnis starten

        cd docker
        docker compose up

   Falls lokal noch ein inkompatibles PostgreSQL-Volume aus einem früheren Start existiert, kann die lokale Umgebung
   zurückgesetzt werden mit:

        docker compose down -v
        docker compose up

2) IntelliJ-Run-Konfigurationen der Services starten
    * "Process Archive Service"
    * "Resource Service"
    * "Inspection Service"
    * "Auth SCS"

   Alternativ können die Services aus dem Projekt-Root per Maven gestartet werden:

        ./mvnw -pl jme-process-archive-auth-scs spring-boot:run -Dspring-boot.run.profiles=local
        ./mvnw -pl jme-process-archive-resource-service spring-boot:run -Dspring-boot.run.profiles=local
        ./mvnw -pl jme-process-archive-service spring-boot:run -Dspring-boot.run.profiles=local
        ./mvnw -pl jme-process-archive-inspection-service spring-boot:run -Dspring-boot.run.profiles=local

3) Verfügung ausstellen

        curl --header "Content-Type: application/json" --request POST --data '{"title":"test-title","someDecreeData":"test-data"}' http://localhost:8082/jme-process-archive-resource-service/api/decrees 

4) Diagramm-Version erstellen

        curl --header "Content-Type: application/json" --request PUT --data '{"name":"test-diagram","graph":"test-graph"}' http://localhost:8082/jme-process-archive-resource-service/api/diagrams/eins

5) Deklaration erstellen

        curl --header "Content-Type: application/json" --request PUT --data '{"payload":"test-payload"}' http://localhost:8082/jme-process-archive-resource-service/api/declarations/test-declaration

Die ausgestellte Verfügung, das erzeugte Verfügungsdokument, die erstellte Diagramm-Version und die erstellte
Deklaration werden automatisch vom Process-Archive-Service archiviert. Für die ausgestellte Verfügung werden dabei
zwei Artefakte archiviert (``Decree`` und ``DecreeSummary``, siehe
[Mehrere Artefakte pro Domain-Event](#mehrere-artefakte-pro-domain-event)), d.h. ein Decree-POST führt insgesamt zu
drei Archiv-Objekten. Die Artefakte zur gleichen Reference-Id lassen sich im ``list``-Endpunkt des Inspection-Service
über das Metadaten-Feld ``schema`` unterscheiden.

6) Backfill lokal testen

   Zuerst muss im Resource-Service ein Artefakt existieren. Für ein ``JmeDecreeDocumentCreatedEvent`` erzeugt der
   Decree-POST automatisch auch ein DecreeDocument:

        DECREE_DOCUMENT_ID=$(curl -s --header "Content-Type: application/json" --request POST \
          --data '{"title":"test-title","someDecreeData":"test-data"}' \
          http://localhost:8082/jme-process-archive-resource-service/api/decrees \
          | jq -r .decreeDocumentId)

   Die ``DECREE_DOCUMENT_ID`` kann anschliessend als ``archiveDataReferences[].id`` verwendet werden.
   Da DecreeDocuments keine fachliche Versionierung kennen, wird ``version`` weggelassen.

   Access Token vom lokalen Auth-SCS holen:

        TOKEN=$(curl -s --request POST http://localhost:8081/jme-process-archive-auth-scs/oauth2/token \
          --header "Content-Type: application/x-www-form-urlencoded" \
          --data "grant_type=client_credentials&client_id=jme-process-archive-it-client&client_secret=secret" \
          | jq -r .access_token)

   Backfill-Job einreichen:

        JOB_ID=$(uuidgen)
        PAS_BACKFILL_JOB=/tmp/pas-backfill-job.yaml
        printf 'message: JmeDecreeDocumentCreatedEvent\narchiveDataReferences:\n  - id: %s\n' \
          "${DECREE_DOCUMENT_ID}" > "${PAS_BACKFILL_JOB}"

   Die erzeugte Datei hat folgendes Format (``message`` entspricht dem Kafka-Event-Typ, die zugehörige
   Archiv-Konfiguration wird daraus aufgelöst; ``archiveDataReferences`` enthält die IDs der zu archivierenden
   Artefakte). Für Artefakte **ohne** fachliche Versionierung (z.B. DecreeDocument) wird ``version`` weggelassen:

        message: JmeDecreeDocumentCreatedEvent
        archiveDataReferences:
          - id: 81572315-ab4d-482e-8254-7b049961f46b

   Für Artefakte **mit** fachlicher Versionierung (z.B. Diagram) wird ``version`` angegeben:

        message: JmeDiagramVersionCreatedEvent
        archiveDataReferences:
          - id: test-diagram
            version: 2

   Es können auch mehrere Referenzen in einem Job angegeben werden:

        message: JmeDiagramVersionCreatedEvent
        archiveDataReferences:
          - id: test-diagram
            version: 2
          - id: another-diagram
            version: 1

   Optional kann der Job die gewünschte Archiv-Konfiguration über deren ``id`` in ``messages.json`` mit dem Feld
   ``config-id`` adressieren. Existieren für eine Message mehrere Remote-Data-Konfigurationen (siehe
   [Mehrere Artefakte pro Domain-Event](#mehrere-artefakte-pro-domain-event)), ist ``config-id`` zwingend:

        message: JmeDecreeDocumentCreatedEvent
        config-id: decree-document
        archiveDataReferences:
          - id: 81572315-ab4d-482e-8254-7b049961f46b

        curl -i --request PUT "http://localhost:8080/process-archive/api/jobs/${JOB_ID}" \
          --header "Authorization: Bearer ${TOKEN}" \
          --header "Content-Type: application/yaml" \
          --data-binary @"${PAS_BACKFILL_JOB}"

   Der Process-Archive-Service speichert den Job und die Tasks in PostgreSQL und publiziert pro Referenz ein
   ``CreateArtifactCommand`` über die Outbox auf Kafka.

   Der gleiche Request kann mit gleicher ``JOB_ID`` erneut gesendet werden. Wenn der Inhalt identisch ist, behandelt der
   Process-Archive-Service den Request idempotent und antwortet wieder mit HTTP 200.

   Backfill-Report als YAML abholen:

        curl --request GET "http://localhost:8080/process-archive/api/jobs/${JOB_ID}/report" \
          --header "Authorization: Bearer ${TOKEN}" \
          --header "Accept: application/yaml"

   Beispiel für einen Report direkt nach der Einreichung:

        message: JmeDecreeDocumentCreatedEvent
        job-state: open
        job-id: 75e32a34-28fc-4de4-9f0e-d1bdf4b69a32
        started: 2026-06-18T10:15:30.123Z
        archiveDataReferences:
        - id: 81572315-ab4d-482e-8254-7b049961f46b
          version: 1
          state: open

Die erzeugten Archiv-Objekte können mit dem Inspection-Service unter Links nach folgenden Mustern eingesehen werden:

* Payload

        http://localhost:8083/jme-process-archive-inspection-service/api/archived-data/payload/bit-jme-processarchive-lockable-obs-local/{key}

* Payload als String (wenn verschlüsselt)

        http://localhost:8083/jme-process-archive-inspection-service/api/archived-data/payload-string/bit-jme-processarchive-lockable-obs-local/{key}

* Metadata

        http://localhost:8083/jme-process-archive-inspection-service/api/archived-data/metadata/bit-jme-processarchive-lockable-obs-local/{key}

* Retention

        http://localhost:8083/jme-process-archive-inspection-service/api/archived-data/retention/bit-jme-processarchive-lockable-obs-local/{key}

* Tags

        http://localhost:8083/jme-process-archive-inspection-service/api/archived-data/tags/bit-jme-processarchive-lockable-obs-local/{key}

* Bucket Lifecycle Configuration

        http://localhost:8083/jme-process-archive-inspection-service/api/archived-data/bit-jme-processarchive-lockable-obs-local/lifecycle/configuration

* List of metadata for objects stored in this bucket during the current day

        http://localhost:8083/jme-process-archive-inspection-service/api/archived-data/bit-jme-processarchive-lockable-obs-local/list

wobei ``{key}`` den Key des Archiv-Objekts im Object-Storage bezeichnet. Dieser hängt von der konfigurierten
Object-Storage-Strategy ab (Prefix-Mode) und kann in diesem Beispiel dem Log-Output des Process-Archive-Service oder dem
Event
``SharedArchivedArtifactVersionCreatedEvent`` entnommen werden.

Soll eine spezifische Version eines Archiv-Objekts zu einem Key ausgelesen werden, müssen die obigen URLs jeweils um
einen Version-Request-Parameter in der Art ``?version=QTkzMjNDN0UtRUEwMC0xMUVCLTgzRTEtOUE1NTAwQkY1Qjgw`` ergänzt werden.
Die Version-ID, unter der ein Archiv-Objekt abgespeichert wurde, kann dem Log-Output des Process-Archive-Service oder
dem Event
``SharedArchivedArtifactVersionCreatedEvent`` entnommen werden.

#### DEV-Umgebung

Wie oben "Lokale Ausführung" beschrieben, aber mit URLs für die DEV-Umgebung und ohne ``docker-compose up``.

1) Verfügung ausstellen

        curl --header "Content-Type: application/json" --request POST --data '{"title":"test-title","someDecreeData":"test-data"}' https://dev-jme-internal.bit.admin.ch/jme-process-archive-resource-service/api/decrees  

2) Diagramm-Version erstellen

        curl --header "Content-Type: application/json" --request PUT --data '{"name":"test-diagram","graph":"test-graph"}' https://dev-jme-internal.bit.admin.ch/jme-process-archive-resource-service/api/diagrams/eins

3) Deklaration erstellen

        curl --header "Content-Type: application/json" --request PUT --data '{"payload":"test-payload"}' https://dev-jme-internal.bit.admin.ch/jme-process-archive-resource-service/api/declarations/test-declaration

4) Archiv inspizieren

* Payload

        https://dev-jme-internal.bit.admin.ch/jme-process-archive-inspection-service/api/archived-data/payload/bit-jme-processarchive-lockable-obs-dev/{key}  

* Payload als String (wenn verschlüsselt)

        https://dev-jme-internal.bit.admin.ch/jme-process-archive-inspection-service/api/archived-data/payload-string/bit-jme-processarchive-lockable-obs-dev/{key}  

* Metadata

        https://dev-jme-internal.bit.admin.ch/jme-process-archive-inspection-service/api/archived-data/metadata/bit-jme-processarchive-lockable-obs-dev/{key}

* Retention

        https://dev-jme-internal.bit.admin.ch/jme-process-archive-inspection-service/api/archived-data/retention/bit-jme-processarchive-lockable-obs-dev/{key}

* Tags

        http://dev-jme-internal.bit.admin.ch/jme-process-archive-inspection-service/api/archived-data/tags/bit-jme-processarchive-lockable-obs-dev/{key}

* Bucket Lifecycle Configuration

        http://dev-jme-internal.bit.admin.ch/jme-process-archive-inspection-service/api/archived-data/bit-jme-processarchive-lockable-obs-dev/lifecycle/configuration

* List of metadata for objects stored in this bucket during the current day

        http://dev-jme-internal.bit.admin.ch/jme-process-archive-inspection-service/api/archived-data/bit-jme-processarchive-lockable-obs-dev/list

#### DEV-Umgebung (AWS)

Auf AWS ist bereits eine laufende Instanz von jme-process-archive-example auf dem bit-jme (dev Stage) Cluster zu finden.

Warnung: In der Regel werden Adressen unter "*.admin.ch" nicht über den Proxy geleitet. Um die unteren Adressen
erreichen
zu können, kann man temporär den Wert "*.admin.ch" aus der no_proxy Regel entfernen. Ansonsten wird die Verbindung
zu den Endpunkten nicht möglich.

1) Verfügung ausstellen

        curl --header "Content-Type: application/json" --request POST --data '{"title":"test-title","someDecreeData":"test-data"}' https://jme-dev.ingress.nivel.bazg.admin.ch/jme-process-archive-resource-service/api/decrees  

2) Diagramm-Version erstellen

        curl --header "Content-Type: application/json" --request PUT --data '{"name":"test-diagram","graph":"test-graph"}' https://jme-dev.ingress.nivel.bazg.admin.ch/jme-process-archive-resource-service/api/diagrams/eins

3) Deklaration erstellen

        curl --header "Content-Type: application/json" --request PUT --data '{"payload":"test-payload"}' https://jme-dev.ingress.nivel.bazg.admin.ch/jme-process-archive-resource-service/api/declarations/test-declaration

4) Archiv inspizieren

* Payload

        https://dev-bit-jme.aws.bazg.admin.ch/jme-process-archive-inspection-service/api/archived-data/payload/bit-jme-processarchive-lockable-obs-dev/{key}  

* Payload als String (wenn verschlüsselt)

        https://dev-bit-jme.aws.bazg.admin.ch/jme-process-archive-inspection-service/api/archived-data/payload-string/bit-jme-processarchive-lockable-obs-dev/{key}  

* Metadata

        https://dev-bit-jme.aws.bazg.admin.ch/jme-process-archive-inspection-service/api/archived-data/metadata/bit-jme-processarchive-lockable-obs-dev/{key}

* Retention

        https://dev-bit-jme.aws.bazg.admin.ch/jme-process-archive-inspection-service/api/archived-data/retention/bit-jme-processarchive-lockable-obs-dev/{key}

* Tags

        https://dev-bit-jme.aws.bazg.admin.ch/jme-process-archive-inspection-service/api/archived-data/tags/bit-jme-processarchive-lockable-obs-dev/{key}

* Bucket Lifecycle Configuration

        https://dev-bit-jme.aws.bazg.admin.ch/jme-process-archive-inspection-service/api/archived-data/bit-jme-processarchive-lockable-obs-dev/lifecycle/configuration

* List of metadata for objects stored in this bucket during the current day

        https://dev-bit-jme.aws.bazg.admin.ch/jme-process-archive-inspection-service/api/archived-data/bit-jme-processarchive-lockable-obs-dev/list

## SearchItems API (OpenSearch)

Der Process Archive Service stellt unter `/index-api/searchitems` einen Endpunkt zur Verfügung, über welchen die im
Object-Store archivierten Daten als SearchItems zurückgegeben werden können.
Die SearchItems enthalten daten zu den archivierten Artefakten, welche in einem OpenSearch-Index gespeichert werden
können, um die Suche nach den Artefakten zu ermöglichen.

Die Parameter, welche in einem Request mitgegeben werden können, sind:

* ``index_type``: Gibt den Typ der SearchItems an, welche zurückgegeben werden sollen. Der Typ der SearchItems bestimmt,
  welche Daten in den SearchItems enthalten sind.
* ``origin_id``: Gibt die Id der SearchItems an, welche zurückgegeben werden sollen. Diese Id entspricht der Bucket-Id
  und dem Key, unter welchen die archivierten Artefakte im Object-Store abgespeichert sind.
* ``origin_version``: Optionaler Parameter, welcher die Version der SearchItems angibt, welche zurückgegeben werden
  sollen. Wenn nicht mitgegeben, werden die aktuellsten Versionen zurückgegeben.

Diese API kann mit Swagger unter folgender URL aufgerufen und getestet werden:

* Local: http://localhost:8080/process-archive/swagger-ui/index.html?urls.primaryName=OpenSearch+SearchItems+API
* DEV
  AWS: https://jme-dev.ingress.nivel.bazg.admin.ch/process-archive/swagger-ui/index.html?urls.primaryName=OpenSearch+SearchItems+API

Diese API ist geschützt, d.h. es muss ein gültiger JWT-Token im Authorization-Header des Requests mitgegeben werden, um
die API aufrufen zu können.
Ein OAuth-Mock-Server ist verfügbar, welcher gültige JWT-Tokens mit der richtigen Rolle ausstellen kann.
