package ch.admin.bit.jeap.jme.processarchive.resource.kafka;

import ch.admin.bit.jeap.jme.processarchive.event.JmeCreateDeclarationCommandBuilder;
import ch.admin.bit.jeap.jme.processarchive.event.JmeDecreeCreatedEventBuilder;
import ch.admin.bit.jeap.jme.processarchive.event.JmeDecreeDocumentCreatedEventBuilder;
import ch.admin.bit.jeap.jme.processarchive.event.JmeDiagramVersionCreatedEventBuilder;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.Decree;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.MessagePublisher;
import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;
import ch.admin.bit.jme.decree.JmeDecreeDocumentCreatedEvent;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
class DomainMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<AvroMessageKey, AvroMessage> kafkaTemplate;

    private final TopicConfiguration topicConfiguration;

    DomainMessagePublisher(KafkaTemplate<AvroMessageKey, AvroMessage> kafkaTemplate, TopicConfiguration topicConfiguration) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicConfiguration = topicConfiguration;
    }

    @Override
    public void decreeCreated(Decree decree, String processId) {
        JmeDecreeCreatedEvent event = JmeDecreeCreatedEventBuilder.createForProcessId(processId)
                .id(decree.getId())
                .title(decree.getTitle())
                .someDecreeData(decree.getSomeDecreeData())
                .createdAt(decree.getCreatedAt())
                .idempotenceId(decree.getId())
                .build();
        send(event, topicConfiguration.getDecreeCreated());
    }

    @Override
    public void decreeDocumentCreated(String decreeDocumentId, String processId) {
        JmeDecreeDocumentCreatedEvent event = JmeDecreeDocumentCreatedEventBuilder.createForProcessId(processId)
                .id(decreeDocumentId)
                .idempotenceId(decreeDocumentId)
                .build();
        send(event, topicConfiguration.getDecreeDocumentCreated());
    }

    @Override
    public void diagramVersionCreated(String diagramId, int diagramVersion, String processId) {
        JmeDiagramVersionCreatedEvent event = JmeDiagramVersionCreatedEventBuilder.createForProcessId(processId)
                .id(diagramId)
                .version(diagramVersion)
                .idempotenceId(diagramId + diagramVersion)
                .build();
        send(event, topicConfiguration.getDiagramVersionCreated());
    }

    @Override
    public void createDeclaration(String declarationId, String processId, String payload) {
        JmeCreateDeclarationCommand command = JmeCreateDeclarationCommandBuilder.createForProcessId(processId)
                .idempotenceId(processId + "#" + declarationId)
                .text(payload)
                .build();
        send(command, JmeCreateDeclarationCommand.TypeRef.DEFAULT_TOPIC);
    }

    private void send(AvroMessage message, String topic) {
        log.debug("Publishing message '{}' to topic '{}'.", message, topic);
        try {
            kafkaTemplate.send(topic, message).get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MessagePublishingException.publishingInterrupted(message, topic, e);
        } catch (ExecutionException e) {
            throw MessagePublishingException.publishingFailed(message, topic, e);
        }
    }

}
