package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.MessageCorrelationProvider;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;

public class CreateDeclarationCommandReferenceProvider implements MessageCorrelationProvider<JmeCreateDeclarationCommand> {

    @Override
    public String getOriginProcessId(JmeCreateDeclarationCommand command) {
        return command.getOptionalProcessId().orElse(null);
    }
}
