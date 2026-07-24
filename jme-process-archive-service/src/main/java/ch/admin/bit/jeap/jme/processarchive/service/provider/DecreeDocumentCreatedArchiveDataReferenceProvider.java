package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataReference;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataReferenceProvider;
import ch.admin.bit.jme.decree.JmeDecreeDocumentCreatedEvent;

public class DecreeDocumentCreatedArchiveDataReferenceProvider implements ArchiveDataReferenceProvider<JmeDecreeDocumentCreatedEvent> {

    @Override
    public ArchiveDataReference getReference(JmeDecreeDocumentCreatedEvent event) {
        String documentId = event.getReferences().getNewDecreeDocument().getId();
        return ArchiveDataReference.builder()
                .id(documentId)
                .build();
    }

}
