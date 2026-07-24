package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveData;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.MessageArchiveDataProvider;
import ch.admin.bit.jeap.processarchive.test.decreesummary.v1.DecreeSummary;
import ch.admin.bit.jeap.processarchive.web.AvroBinarySerializer;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;

/**
 * Second archive data provider registered for the {@link JmeDecreeCreatedEvent}: besides the Decree archived by
 * {@link DecreeCreatedDataProvider}, this provider archives a DecreeSummary as an additional artifact of the same
 * domain event. The two artifacts share the same reference id (the decree id) but use different archive types, which
 * keeps their idempotence ids distinct.
 */
public class DecreeSummaryDataProvider implements MessageArchiveDataProvider<JmeDecreeCreatedEvent> {

    private final AvroBinarySerializer avroBinarySerializer;

    public DecreeSummaryDataProvider() {
        this.avroBinarySerializer = new AvroBinarySerializer();
    }

    @Override
    public ArchiveData getArchiveData(JmeDecreeCreatedEvent jmeDecreeCreatedEvent) {
        return ArchiveData.builder()
                .referenceId(jmeDecreeCreatedEvent.getReferences().getNewDecree().getId())
                .system("JME")
                .schema("DecreeSummary")
                .schemaVersion(1)
                .contentType("avro/binary")
                .payload(createPayload(jmeDecreeCreatedEvent))
                .build();
    }

    @SneakyThrows
    private byte[] createPayload(JmeDecreeCreatedEvent event) {
        DecreeSummary decreeSummary = DecreeSummary.newBuilder()
                .setDecreeId(event.getReferences().getNewDecree().getId())
                .setTitle(event.getPayload().getTitle())
                .setCreatedAt(event.getPayload().getCreatedAt())
                .build();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            avroBinarySerializer.serialize(decreeSummary, outputStream);
            return outputStream.toByteArray();
        }
    }
}
