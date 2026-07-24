package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveData;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.MessageArchiveDataProvider;
import ch.admin.bit.jme.declaration.CreateDeclarationPayload;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provides an example of a {@link MessageArchiveDataProvider} that creates {@link ArchiveData} for a
 * {@link ch.admin.bit.jeap.command.Command}.
 * <p>
 * It also demonstrates support for non-avro archive data.
 */
public class CreateDeclarationCommandDataProvider implements MessageArchiveDataProvider<JmeCreateDeclarationCommand> {

    @Override
    public ArchiveData getArchiveData(JmeCreateDeclarationCommand command) {
        return ArchiveData.builder()
                .referenceId(command.getIdentity().getIdempotenceId())
                .system("JME")
                .schema("JsonExample")
                .schemaVersion(1)
                .contentType("application/json")
                .payload(createJson(command.getPayload()))
                .build();
    }

    private byte[] createJson(CreateDeclarationPayload payload) {
        String text = payload.getText().trim().replaceAll("\\s+", " ");
        return """
                {
                    "text": "%s"
                }
                """.formatted(text).getBytes(UTF_8);
    }
}
