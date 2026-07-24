package ch.admin.bit.jeap.jme.processarchive.event;

import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEventPayload;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEventReferences;
import ch.admin.bit.jme.decree.JmeDecreeReference;

import java.time.ZonedDateTime;

public class JmeDecreeCreatedEventBuilder extends AbstractExampleEventBuilder<JmeDecreeCreatedEventBuilder, JmeDecreeCreatedEvent> {

    private String id;
    private String title;
    private String someDecreeData;
    private ZonedDateTime createdAt;

    private JmeDecreeCreatedEventBuilder(String processId) {
        super(JmeDecreeCreatedEvent::new, processId);
    }

    public static JmeDecreeCreatedEventBuilder createForProcessId(String processId) {
        return new JmeDecreeCreatedEventBuilder(processId);
    }

    public JmeDecreeCreatedEventBuilder id(String id) {
        this.id = id;
        return this;
    }

    public JmeDecreeCreatedEventBuilder title(String title) {
        this.title = title;
        return this;
    }

    public JmeDecreeCreatedEventBuilder someDecreeData(String someDecreeData) {
        this.someDecreeData = someDecreeData;
        return this;
    }

    public JmeDecreeCreatedEventBuilder createdAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public JmeDecreeCreatedEvent build() {
        if (id == null) {
            throw AvroMessageBuilderException.propertyNull("id");
        }
        if (title == null) {
            throw AvroMessageBuilderException.propertyNull("title");
        }
        if (someDecreeData == null) {
            throw AvroMessageBuilderException.propertyNull("someDecreeData");
        }
        if (createdAt == null) {
            throw AvroMessageBuilderException.propertyNull("createdAt");
        }

        JmeDecreeReference newDecree = JmeDecreeReference.newBuilder()
                .setId(id)
                .build();
        JmeDecreeCreatedEventReferences references = JmeDecreeCreatedEventReferences.newBuilder()
                .setNewDecree(newDecree)
                .build();
        setReferences(references);

        JmeDecreeCreatedEventPayload payload = JmeDecreeCreatedEventPayload.newBuilder()
                .setTitle(title)
                .setSomeDecreeData(someDecreeData)
                .setCreatedAt(createdAt.toInstant())
                .build();
        setPayload(payload);

        return super.build();
    }
}
