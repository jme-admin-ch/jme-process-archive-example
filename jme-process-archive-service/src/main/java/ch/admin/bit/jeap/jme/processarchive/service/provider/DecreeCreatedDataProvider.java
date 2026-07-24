package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.ArchiveData;
import ch.admin.bit.jeap.processarchive.plugin.api.archivedata.MessageArchiveDataProvider;
import ch.admin.bit.jeap.processarchive.test.DecreeReference;
import ch.admin.bit.jeap.processarchive.test.decree.v3.Decree;
import ch.admin.bit.jeap.processarchive.web.AvroBinarySerializer;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEvent;
import ch.admin.bit.jme.decree.JmeDecreeCreatedEventPayload;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class DecreeCreatedDataProvider implements MessageArchiveDataProvider<JmeDecreeCreatedEvent> {

    private final AvroBinarySerializer avroBinarySerializer;

    public DecreeCreatedDataProvider() {
        this.avroBinarySerializer = new AvroBinarySerializer();
    }

    @Override
    public ArchiveData getArchiveData(JmeDecreeCreatedEvent jmeDecreeCreatedEvent) {
        return ArchiveData.builder()
                .referenceId(jmeDecreeCreatedEvent.getReferences().getNewDecree().getId())
                .system("JME")
                .schema("Decree")
                .schemaVersion(3)
                .contentType("avro/binary")
                .payload(createPayload(jmeDecreeCreatedEvent.getPayload()))
                .build();
    }

    @SneakyThrows
    private byte[] createPayload(JmeDecreeCreatedEventPayload payload) {
        DecreeReference example = DecreeReference.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setType("Example")
                .build();
        Decree decree = Decree.newBuilder()
                .setTitle(payload.getTitle())
                .setPayload(payload.getSomeDecreeData())
                .setCreatedAt(payload.getCreatedAt())
                .setDecreeReference(example)
                .build();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            avroBinarySerializer.serialize(decree, outputStream);
            return outputStream.toByteArray();
        }
    }
}
