package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.MessageCorrelationProvider;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;

public class DecreeCorrelationProvider implements MessageCorrelationProvider<JmeDecreeCreatedEvent> {

    /**
     * Example of a MessageCorrelationProvider implementation. This example returns the processId of the message,
     * which is the default behavior.
     * An actual implementation would extract the origin process id from the message payload.
     * This implementation only demonstrates the use of such a provider.
     */
    @Override
    public String getOriginProcessId(JmeDecreeCreatedEvent event) {
        return event.getProcessId();
    }
}
