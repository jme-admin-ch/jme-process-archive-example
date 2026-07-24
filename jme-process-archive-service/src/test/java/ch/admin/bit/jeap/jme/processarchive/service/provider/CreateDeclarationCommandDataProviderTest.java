package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.jme.processarchive.event.JmeCreateDeclarationCommandBuilder;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveData;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class CreateDeclarationCommandDataProviderTest {

    private static final String PROCESS_ID = "process-123";
    private static final String IDEMPOTENCE_ID = "idempotence-456";
    private static final String TEXT = "Some declaration text";

    @Test
    void testGetArchiveData() {
        final CreateDeclarationCommandDataProvider provider = new CreateDeclarationCommandDataProvider();
        final ArchiveData archiveData = provider.getArchiveData(createCommand());

        assertThat(archiveData.getReferenceId()).isEqualTo(IDEMPOTENCE_ID);
        assertThat(archiveData.getSystem()).isEqualTo("JME");
        assertThat(archiveData.getSchema()).isEqualTo("JsonExample");
        assertThat(archiveData.getSchemaVersion()).isEqualTo(1);
        assertThat(archiveData.getContentType()).isEqualTo("application/json");
        assertThat(archiveData.getVersion()).isNull();
        assertThat(archiveData.getStorageBucket()).isEmpty();
        assertThat(archiveData.getStoragePrefix()).isEmpty();
        assertThat(archiveData.getMetadata()).isEmpty();

        String json = new String(archiveData.getPayload(), UTF_8);
        assertThat(json).contains("\"text\": \"Some declaration text\"");
    }

    @Test
    void testGetArchiveDataNormalizesWhitespace() {
        final CreateDeclarationCommandDataProvider provider = new CreateDeclarationCommandDataProvider();
        JmeCreateDeclarationCommand command = JmeCreateDeclarationCommandBuilder.createForProcessId(PROCESS_ID)
                .text("  multiple   spaces\tand\ttabs  ")
                .idempotenceId(IDEMPOTENCE_ID)
                .build();

        final ArchiveData archiveData = provider.getArchiveData(command);

        String json = new String(archiveData.getPayload(), UTF_8);
        assertThat(json).contains("\"text\": \"multiple spaces and tabs\"");
    }

    private JmeCreateDeclarationCommand createCommand() {
        return JmeCreateDeclarationCommandBuilder.createForProcessId(PROCESS_ID)
                .text(TEXT)
                .idempotenceId(IDEMPOTENCE_ID)
                .build();
    }
}
