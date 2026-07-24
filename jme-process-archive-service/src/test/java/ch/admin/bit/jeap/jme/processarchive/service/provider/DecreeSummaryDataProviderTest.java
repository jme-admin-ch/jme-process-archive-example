package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.jme.processarchive.event.JmeDecreeCreatedEventBuilder;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveData;
import ch.admin.bit.jeap.processarchive.test.decreesummary.v1.DecreeSummary;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;
import lombok.SneakyThrows;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DecreeSummaryDataProviderTest {

    private static final String PROCESS_ID = "12345";
    private static final String DECREE_ID = "decree-id";
    private static final String DECREE_TITLE = "test title";
    private static final String DECREE_DATA = "some test data";
    private static final ZonedDateTime DECREE_CREATED_AT = ZonedDateTime.now();

    @Test
    void testGetArchiveData() {
        final DecreeSummaryDataProvider decreeSummaryDataProvider = new DecreeSummaryDataProvider();
        final ArchiveData archiveData = decreeSummaryDataProvider.getArchiveData(createJmeDecreeCreatedEvent());
        assertThat(archiveData.getReferenceId()).isEqualTo(DECREE_ID);
        assertThat(archiveData.getVersion()).isNull();
        assertThat(archiveData.getContentType()).isEqualTo("avro/binary");
        assertThat(archiveData.getSystem()).isEqualTo("JME");
        assertThat(archiveData.getSchema()).isEqualTo("DecreeSummary");
        assertThat(archiveData.getSchemaVersion()).isEqualTo(1);
        assertThat(archiveData.getStorageBucket()).isEmpty();
        assertThat(archiveData.getStoragePrefix()).isEmpty();
        assertThat(archiveData.getMetadata()).isEmpty();
        final DecreeSummary decreeSummary = decodePayload(archiveData.getPayload());
        assertThat(decreeSummary.getDecreeId()).isEqualTo(DECREE_ID);
        assertThat(decreeSummary.getTitle()).isEqualTo(DECREE_TITLE);
        assertThat(decreeSummary.getCreatedAt().atZone(DECREE_CREATED_AT.getZone()).toEpochSecond())
                .isEqualTo(DECREE_CREATED_AT.toEpochSecond());
    }

    @Test
    void testArchiveDataDiffersFromDecreeArchiveData() {
        // Both providers are registered for the same JmeDecreeCreatedEvent in messages.json. The resulting artifacts
        // share the reference id but must differ in the archive type (schema) for their idempotence ids to be distinct.
        final JmeDecreeCreatedEvent event = createJmeDecreeCreatedEvent();
        final ArchiveData decree = new DecreeCreatedDataProvider().getArchiveData(event);
        final ArchiveData decreeSummary = new DecreeSummaryDataProvider().getArchiveData(event);
        assertThat(decreeSummary.getReferenceId()).isEqualTo(decree.getReferenceId());
        assertThat(decreeSummary.getSystem()).isEqualTo(decree.getSystem());
        assertThat(decreeSummary.getSchema()).isNotEqualTo(decree.getSchema());
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
    private DecreeSummary decodePayload(byte[] payload) {
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(payload, null);
        DatumReader<DecreeSummary> reader = new SpecificDatumReader<>(DecreeSummary.class);
        return reader.read(null, decoder);
    }
}
