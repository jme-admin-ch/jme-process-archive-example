package ch.admin.bit.jeap.jme.processarchive.service.condition;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataCondition;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEvent;

/**
 * Example of an {@link ArchiveDataCondition} that controls whether data archiving is triggered by an event or not.
 * This simple example condition will only archive diagram data if the diagram ID does not contain "ignored".
 */
public class ArchiveDiagramCondition implements ArchiveDataCondition<JmeDiagramVersionCreatedEvent> {

    @Override
    public boolean isArchiveDataForMessage(JmeDiagramVersionCreatedEvent event) {
        return !event.getReferences().getNewDiagramVersion().getId().contains("ignored");
    }
}
