package ch.admin.bit.jeap.jme.processarchive.resource.kafka;

import ch.admin.bit.jeap.event.shared.processarchive.archivedartifactversioncreated.SharedArchivedArtifactVersionCreatedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageConsumer {

    @Getter
    private final Collection<SharedArchivedArtifactVersionCreatedEvent> messages = new CircularFifoQueue<>(100);

    @Getter
    private final Collection<UUID> messageIds = new CircularFifoQueue<>(100);

    @KafkaListener(topics = "jme-process-archive-artifactversioncreated")
    public void listenToReceivedEvent(SharedArchivedArtifactVersionCreatedEvent event, Acknowledgment acknowledgment) {
        messageIds.add(UUID.fromString(event.getReferences().getArchivedArtifact().getReferenceId()));
        messages.add(event);
        acknowledgment.acknowledge();
    }
}
