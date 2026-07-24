package ch.admin.bit.jeap.jme.processarchive.event;

import ch.admin.bit.jeap.command.avro.AvroCommandBuilder;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.declaration.CreateDeclarationPayload;
import ch.admin.bit.jme.declaration.CreateDeclarationReferences;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;

public class JmeCreateDeclarationCommandBuilder extends AvroCommandBuilder<JmeCreateDeclarationCommandBuilder, JmeCreateDeclarationCommand> {

    private String text;

    private JmeCreateDeclarationCommandBuilder() {
        super(JmeCreateDeclarationCommand::new);
    }


    public static JmeCreateDeclarationCommandBuilder createForProcessId(String processId) {
        JmeCreateDeclarationCommandBuilder builder = new JmeCreateDeclarationCommandBuilder();
        builder.setProcessId(processId);
        return builder;
    }

    @Override
    protected final String getServiceName() {
        return "jme-process-archive-resource-service";
    }

    @Override
    protected final String getSystemName() {
        return "JME";
    }

    // Define the additional builder methods.
    public JmeCreateDeclarationCommandBuilder text(String text) {
        this.text = text;
        return this;
    }

    @Override
    protected JmeCreateDeclarationCommandBuilder self() {
        return this;
    }

    @Override
    public JmeCreateDeclarationCommand build() {
        if (this.text == null) {
            throw AvroMessageBuilderException.propertyNull("JmeCreateDeclarationCommand.text");
        }
        if (this.idempotenceId == null) {
            throw AvroMessageBuilderException.propertyNull("JmeCreateDeclarationCommand.idempotenceId");
        }
        CreateDeclarationReferences references = CreateDeclarationReferences.newBuilder().build();
        CreateDeclarationPayload payload = CreateDeclarationPayload.newBuilder()
                .setText(text)
                .build();
        setReferences(references);
        setPayload(payload);
        return super.build();
    }
}
