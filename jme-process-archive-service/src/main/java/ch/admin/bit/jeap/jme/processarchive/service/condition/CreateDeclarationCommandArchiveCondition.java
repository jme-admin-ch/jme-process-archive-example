package ch.admin.bit.jeap.jme.processarchive.service.condition;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveDataCondition;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;

public class CreateDeclarationCommandArchiveCondition implements ArchiveDataCondition<JmeCreateDeclarationCommand> {

    @Override
    public boolean isArchiveDataForMessage(JmeCreateDeclarationCommand message) {
        return message.getOptionalProcessId().isPresent();
    }
}
