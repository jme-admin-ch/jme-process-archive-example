package ch.admin.bit.jeap.jme.processarchive.event;

import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.decree.JmeDecreeDocumentCreatedEvent;
import ch.admin.bit.jme.decree.JmeDecreeDocumentCreatedEventReferences;
import ch.admin.bit.jme.decree.JmeDecreeDocumentReference;

public class JmeDecreeDocumentCreatedEventBuilder extends AbstractExampleEventBuilder<JmeDecreeDocumentCreatedEventBuilder, JmeDecreeDocumentCreatedEvent> {

    private String id;

    private JmeDecreeDocumentCreatedEventBuilder(String processId) {
        super(JmeDecreeDocumentCreatedEvent::new, processId);
    }

    public static JmeDecreeDocumentCreatedEventBuilder createForProcessId(String processId) {
        return new JmeDecreeDocumentCreatedEventBuilder(processId);
    }

    public JmeDecreeDocumentCreatedEventBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public JmeDecreeDocumentCreatedEvent build() {
        if (id == null) {
            throw AvroMessageBuilderException.propertyNull("id");
        }

        JmeDecreeDocumentReference newDecreeDocument = JmeDecreeDocumentReference.newBuilder()
                .setId(id)
                .build();
        JmeDecreeDocumentCreatedEventReferences references = JmeDecreeDocumentCreatedEventReferences.newBuilder()
                .setNewDecreeDocument(newDecreeDocument)
                .build();
        setReferences(references);

        return super.build();
    }
}
