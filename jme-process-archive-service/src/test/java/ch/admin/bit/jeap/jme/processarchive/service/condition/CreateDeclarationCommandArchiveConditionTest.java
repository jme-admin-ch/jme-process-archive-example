package ch.admin.bit.jeap.jme.processarchive.service.condition;

import ch.admin.bit.jeap.jme.processarchive.event.JmeCreateDeclarationCommandBuilder;
import ch.admin.bit.jeap.messaging.avro.security.AvroClassSecurity;
import ch.admin.bit.jme.declaration.JmeCreateDeclarationCommand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDeclarationCommandArchiveConditionTest {

    private static final String PROCESS_ID = "process-123";
    private static final String IDEMPOTENCE_ID = "idempotence-456";
    private static final String TEXT = "Some declaration text";

    private final CreateDeclarationCommandArchiveCondition condition = new CreateDeclarationCommandArchiveCondition();

    @BeforeAll
    static void installAvroClassWhitelist() {
        // No Spring context installs the Avro class whitelist here, and Avro refuses to resolve any class from a
        // schema that is not trusted. The message types used below are covered by the default whitelist.
        AvroClassSecurity.installDefaultIfMissing();
    }

    @Test
    void isArchiveDataForMessage_withProcessId_returnsTrue() {
        JmeCreateDeclarationCommand command = JmeCreateDeclarationCommandBuilder.createForProcessId(PROCESS_ID)
                .text(TEXT)
                .idempotenceId(IDEMPOTENCE_ID)
                .build();

        assertThat(condition.isArchiveDataForMessage(command)).isTrue();
    }

    @Test
    void isArchiveDataForMessage_withoutProcessId_returnsFalse() {
        JmeCreateDeclarationCommand command = JmeCreateDeclarationCommandBuilder.createForProcessId(null)
                .text(TEXT)
                .idempotenceId(IDEMPOTENCE_ID)
                .build();

        assertThat(condition.isArchiveDataForMessage(command)).isFalse();
    }
}
