package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.jme.processarchive.event.JmeDecreeDocumentCreatedEventBuilder;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataReference;
import ch.admin.bit.jme.decree.JmeDecreeDocumentCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecreeDocumentCreatedArchiveDataReferenceProviderTest {

    private static final String DECREE_DOCUMENT_ID = "1234567890";

    @Test
    void testGetReference() {
        final DecreeDocumentCreatedArchiveDataReferenceProvider decreeDocumentCreatedArchiveDataReferenceProvider = new DecreeDocumentCreatedArchiveDataReferenceProvider();
        final JmeDecreeDocumentCreatedEvent jmeDecreeDocumentCreatedEvent = JmeDecreeDocumentCreatedEventBuilder
                .createForProcessId(DECREE_DOCUMENT_ID)
                .id(DECREE_DOCUMENT_ID)
                .idempotenceId(DECREE_DOCUMENT_ID)
                .build();
        final ArchiveDataReference archiveDataReference = decreeDocumentCreatedArchiveDataReferenceProvider.getReference(jmeDecreeDocumentCreatedEvent);

        assertThat(archiveDataReference.getId()).isEqualTo(DECREE_DOCUMENT_ID);
        assertThat(archiveDataReference.getVersion()).isNull();
    }

}
