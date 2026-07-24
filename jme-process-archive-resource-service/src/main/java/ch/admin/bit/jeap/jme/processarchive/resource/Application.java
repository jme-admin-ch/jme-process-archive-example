package ch.admin.bit.jeap.jme.processarchive.resource;

import ch.admin.bit.jeap.event.shared.processarchive.archivedartifactversioncreated.SharedArchivedArtifactVersionCreatedEvent;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContract;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageProducerContracts;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;
import ch.admin.bit.jme.decree.JmeDecreeDocumentCreatedEvent;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@JeapMessageProducerContracts({
        JmeDecreeDocumentCreatedEvent.TypeRef.class,
        JmeDecreeCreatedEvent.TypeRef.class,
        JmeDiagramVersionCreatedEvent.TypeRef.class,
        JmeCreateDeclarationCommand.TypeRef.class})
@JeapMessageConsumerContract(value = SharedArchivedArtifactVersionCreatedEvent.TypeRef.class, topic = "jme-process-archive-artifactversioncreated")
public class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
