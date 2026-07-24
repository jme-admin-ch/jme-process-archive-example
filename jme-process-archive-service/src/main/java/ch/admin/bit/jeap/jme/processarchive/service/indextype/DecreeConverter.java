package ch.admin.bit.jeap.jme.processarchive.service.indextype;

import ch.admin.bit.jeap.opensearch.indextype.Origin;
import ch.admin.bit.jeap.opensearch.indextype.SearchItem;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;
import ch.admin.bit.jeap.processarchive.plugin.api.indextype.ArchiveTypeToSearchItemConverter;
import ch.admin.bit.jeap.processarchive.test.decree.v3.Decree;
import ch.admin.bit.jme.opensearch.index.jme.decree.JmeDecreeDataV1;
import ch.admin.bit.jme.opensearch.index.jme.decree.JmeDecreeIndexTypeV1;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
public class DecreeConverter implements ArchiveTypeToSearchItemConverter<Decree> {

    @Override
    public SearchItemContainer convert(Decree archivePayload, String archiveId, String version, Map<String, String> metadata) {

        JmeDecreeDataV1 decreeDataV1 = new JmeDecreeDataV1(
                archivePayload.getTitle(),
                archivePayload.getPayload(),
                new JmeDecreeDataV1.DecreeReference(archivePayload.getDecreeReference().getType(), archivePayload.getDecreeReference().getId()),
                archivePayload.getCreatedAt());

        Origin searchItemOrigin = createOrigin(metadata.get("reference-id"), version, archiveId);
        SearchItem<JmeDecreeDataV1> searchItem = new SearchItem<>(searchItemOrigin, decreeDataV1);
        return new SearchItemContainer(JmeDecreeIndexTypeV1.INSTANCE.majorVersion(), JmeDecreeIndexTypeV1.INSTANCE.minorVersion(), searchItem);
    }

    private static Origin createOrigin(String id, String version, String reference) {
        return new Origin(id, version, null, null, Instant.now(), null, Map.of("url", reference));
    }

}
