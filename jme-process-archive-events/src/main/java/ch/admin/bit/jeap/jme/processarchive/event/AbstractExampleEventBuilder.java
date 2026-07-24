package ch.admin.bit.jeap.jme.processarchive.event;

import ch.admin.bit.jeap.domainevent.avro.AvroDomainEvent;
import ch.admin.bit.jeap.domainevent.avro.AvroDomainEventBuilder;

import java.util.function.Supplier;

abstract class AbstractExampleEventBuilder<BuilderType extends AvroDomainEventBuilder<BuilderType, EventType>, EventType extends AvroDomainEvent>
        extends AvroDomainEventBuilder<BuilderType, EventType> {

    protected AbstractExampleEventBuilder(Supplier<EventType> constructor, String processId) {
        super(constructor);
        setProcessId(processId);
    }

    @Override
    protected final String getServiceName() {
        return "jme-process-archive-resource-service";
    }

    @Override
    protected final String getSystemName() {
        return "JME";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final BuilderType self() {
        return (BuilderType) this;
    }

}
