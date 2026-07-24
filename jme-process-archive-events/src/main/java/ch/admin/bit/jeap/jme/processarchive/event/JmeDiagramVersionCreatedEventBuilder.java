package ch.admin.bit.jeap.jme.processarchive.event;

import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.diagram.JmeDiagramReference;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEvent;
import ch.admin.bit.jme.diagram.JmeDiagramVersionCreatedEventReferences;

public class JmeDiagramVersionCreatedEventBuilder extends AbstractExampleEventBuilder<JmeDiagramVersionCreatedEventBuilder, JmeDiagramVersionCreatedEvent> {

    private String id;
    private Integer version;

    private JmeDiagramVersionCreatedEventBuilder(String processId) {
        super(JmeDiagramVersionCreatedEvent::new, processId);
    }

    public static JmeDiagramVersionCreatedEventBuilder createForProcessId(String processId) {
        return new JmeDiagramVersionCreatedEventBuilder(processId);
    }

    public JmeDiagramVersionCreatedEventBuilder id(String id) {
        this.id = id;
        return this;
    }

    public JmeDiagramVersionCreatedEventBuilder version(int version) {
        this.version = version;
        return this;
    }

    @Override
    public JmeDiagramVersionCreatedEvent build() {
        if (id == null) {
            throw AvroMessageBuilderException.propertyNull("id");
        }
        if (version == null) {
            throw AvroMessageBuilderException.propertyNull("version");
        }

        JmeDiagramReference newDiagramVersion = JmeDiagramReference.newBuilder()
                .setId(id)
                .setVersion(version.toString())
                .build();
        JmeDiagramVersionCreatedEventReferences references = JmeDiagramVersionCreatedEventReferences.newBuilder()
                .setNewDiagramVersion(newDiagramVersion)
                .build();
        setReferences(references);

        return super.build();
    }
}
