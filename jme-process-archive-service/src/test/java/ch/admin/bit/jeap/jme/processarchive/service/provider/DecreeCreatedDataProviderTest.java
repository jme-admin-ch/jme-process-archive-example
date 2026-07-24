package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.jme.processarchive.event.JmeDecreeCreatedEventBuilder;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveData;
import ch.admin.bit.jeap.processarchive.test.decree.v3.Decree;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;
import lombok.SneakyThrows;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DecreeCreatedDataProviderTest {

    private static final String PROCESS_ID = "12345";
    private static final String DECREE_ID = "test title";
    private static final String DECREE_TITLE = "test title";
    private static final String DECREE_DATA = "some test data";
    private static final ZonedDateTime DECREE_CREATED_AT = ZonedDateTime.now();

    @Test
    void testGetArchiveData() {
        final DecreeCreatedDataProvider decreeCreatedDataProvider = new DecreeCreatedDataProvider();
        final ArchiveData archiveData = decreeCreatedDataProvider.getArchiveData(createJmeDecreeCreatedEvent());
        assertThat(archiveData.getReferenceId()).isEqualTo(DECREE_ID);
        assertThat(archiveData.getVersion()).isNull();
        assertThat(archiveData.getContentType()).isEqualTo("avro/binary");
        assertThat(archiveData.getSystem()).isEqualTo("JME");
        assertThat(archiveData.getSchema()).isEqualTo("Decree");
        assertThat(archiveData.getSchemaVersion()).isEqualTo(3);
        assertThat(archiveData.getStorageBucket()).isEmpty();
        assertThat(archiveData.getStoragePrefix()).isEmpty();
        assertThat(archiveData.getMetadata()).isEmpty();
        final Decree decree = decodePayload(archiveData.getPayload());
        assertThat(decree.getTitle()).isEqualTo(DECREE_TITLE);
        assertThat(decree.getPayload()).isEqualTo(DECREE_DATA);
        assertThat(decree.getCreatedAt().atZone(DECREE_CREATED_AT.getZone()).toEpochSecond())
                .isEqualTo(DECREE_CREATED_AT.toEpochSecond());
    }

    private JmeDecreeCreatedEvent createJmeDecreeCreatedEvent() {
        return JmeDecreeCreatedEventBuilder.createForProcessId(PROCESS_ID)
                .id(DECREE_ID)
                .title(DECREE_TITLE)
                .someDecreeData(DECREE_DATA)
                .createdAt(DECREE_CREATED_AT)
                .idempotenceId(DECREE_ID)
                .build();
    }

    @SneakyThrows
    private Decree decodePayload(byte[] payload) {
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(payload, null);
        DatumReader<Decree> reader = new SpecificDatumReader<>(Decree.class);
        return reader.read(null, decoder);
    }
}
