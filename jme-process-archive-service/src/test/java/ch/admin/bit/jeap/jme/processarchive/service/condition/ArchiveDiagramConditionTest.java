package ch.admin.bit.jeap.jme.processarchive.service.condition;

import ch.admin.bit.jeap.jme.processarchive.event.JmeDiagramVersionCreatedEventBuilder;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveDiagramConditionTest {

    private static final String PROCESS_ID = "process-123";
    private static final int VERSION = 1;

    private final ArchiveDiagramCondition condition = new ArchiveDiagramCondition();

    @Test
    void isArchiveDataForMessage_regularDiagramId_returnsTrue() {
        JmeDiagramVersionCreatedEvent event = createEvent("diagram-42");

        assertThat(condition.isArchiveDataForMessage(event)).isTrue();
    }

    @Test
    void isArchiveDataForMessage_diagramIdContainsIgnored_returnsFalse() {
        JmeDiagramVersionCreatedEvent event = createEvent("diagram-ignored-42");

        assertThat(condition.isArchiveDataForMessage(event)).isFalse();
    }

    @Test
    void isArchiveDataForMessage_diagramIdExactlyIgnored_returnsFalse() {
        JmeDiagramVersionCreatedEvent event = createEvent("ignored");

        assertThat(condition.isArchiveDataForMessage(event)).isFalse();
    }

    private JmeDiagramVersionCreatedEvent createEvent(String diagramId) {
        return JmeDiagramVersionCreatedEventBuilder.createForProcessId(PROCESS_ID)
                .id(diagramId)
                .idempotenceId(diagramId)
                .version(VERSION)
                .build();
    }
}
