package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.jme.processarchive.event.JmeDiagramVersionCreatedEventBuilder;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataReference;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiagramVersionCreatedArchiveDataReferenceProviderTest {

    private static final String DIAGRAM_ID = "1234567890";
    private static final int DIAGRAM_VERSION = 1;

    @Test
    void testGetReference() {
        final DiagramVersionCreatedArchiveDataReferenceProvider diagramVersionCreatedArchiveDataReferenceProvider = new DiagramVersionCreatedArchiveDataReferenceProvider();
        final JmeDiagramVersionCreatedEvent jmeDiagramVersionCreatedEvent = JmeDiagramVersionCreatedEventBuilder
                .createForProcessId(DIAGRAM_ID)
                .id(DIAGRAM_ID)
                .idempotenceId(DIAGRAM_ID)
                .version(DIAGRAM_VERSION)
                .build();

        final ArchiveDataReference archiveDataReference = diagramVersionCreatedArchiveDataReferenceProvider.getReference(jmeDiagramVersionCreatedEvent);

        assertThat(archiveDataReference.getId()).isEqualTo(DIAGRAM_ID);
        assertThat(archiveDataReference.getVersion()).isEqualTo(Integer.valueOf(DIAGRAM_VERSION));
    }

}
