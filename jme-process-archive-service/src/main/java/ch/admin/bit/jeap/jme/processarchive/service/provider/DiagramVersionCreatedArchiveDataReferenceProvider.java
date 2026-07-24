package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataReference;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataReferenceProvider;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEvent;

public class DiagramVersionCreatedArchiveDataReferenceProvider implements ArchiveDataReferenceProvider<JmeDiagramVersionCreatedEvent> {

    @Override
    public ArchiveDataReference getReference(JmeDiagramVersionCreatedEvent event) {
        return ArchiveDataReference.builder()
                .id(event.getReferences().getNewDiagramVersion().getId())
                .version(Integer.valueOf(event.getReferences().getNewDiagramVersion().getVersion()))
                .build();
    }
}
