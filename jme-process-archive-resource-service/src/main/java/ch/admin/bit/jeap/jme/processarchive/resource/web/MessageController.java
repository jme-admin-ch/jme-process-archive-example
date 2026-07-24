package ch.admin.bit.jeap.jme.processarchive.resource.web;

import ch.admin.bit.jeap.jme.processarchive.resource.kafka.MessageConsumer;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageConsumer messageConsumer;

    @GetMapping(value = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List the reference IDs of the last 100 SharedArchivedArtifactVersionCreatedEvents")
    public String[] getReceivedInternalMessageIds() {
        return messageConsumer.getMessageIds().stream()
                .map(UUID::toString)
                .toList()
                .toArray(String[]::new);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Return the data of all SharedArchivedArtifactVersionCreatedEvents with this reference id")
    public List<MessageDto> getReceivedInternalMessagesById(@PathVariable String id) {
        List<MessageDto> messages = messageConsumer.getMessages().stream()
                .filter(m -> m.getReferences().getArchivedArtifact().getReferenceId().equals(id))
                .map(m -> new MessageDto(id, m.getType().getName(), m.getType().getVariant()))
                .toList();
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("Message with id " + id + " not found");
        }
        return messages;
    }
}
