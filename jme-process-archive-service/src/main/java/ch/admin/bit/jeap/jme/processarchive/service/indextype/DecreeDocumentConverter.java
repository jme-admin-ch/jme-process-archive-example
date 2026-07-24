package ch.admin.bit.jeap.jme.processarchive.service.indextype;

import ch.admin.bit.jeap.opensearch.indextype.Origin;
import ch.admin.bit.jeap.opensearch.indextype.SearchItem;
import ch.admin.bit.jeap.opensearch.searchitem.model.SearchItemContainer;
import ch.admin.bit.jeap.processarchive.plugin.api.indextype.ArchiveTypeToSearchItemConverter;
import ch.admin.bit.jeap.processarchive.test.decreedocument.v1.DecreeDocument;
import ch.admin.bit.jme.opensearch.index.jme.decreedocument.JmeDecreeDocumentDataV1;
import ch.admin.bit.jme.opensearch.index.jme.decreedocument.JmeDecreeDocumentIndexTypeV1;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
public class DecreeDocumentConverter implements ArchiveTypeToSearchItemConverter<DecreeDocument> {

    @Override
    public SearchItemContainer convert(DecreeDocument archivePayload, String archiveId, String version, Map<String, String> metadata) {

        JmeDecreeDocumentDataV1 decreeDocumentDataV1 = new JmeDecreeDocumentDataV1(
                archivePayload.getDocumentId(),
                new JmeDecreeDocumentDataV1.DecreeReference(archivePayload.getDecreeReference().getType(), archivePayload.getDecreeReference().getId()),
                archivePayload.getCreatedAt());

        Origin searchItemOrigin = createOrigin(metadata.get("reference-id"), version, archiveId);
        SearchItem<JmeDecreeDocumentDataV1> searchItem = new SearchItem<>(searchItemOrigin, decreeDocumentDataV1);
        return new SearchItemContainer(JmeDecreeDocumentIndexTypeV1.INSTANCE.majorVersion(), JmeDecreeDocumentIndexTypeV1.INSTANCE.minorVersion(), searchItem);
    }

    private static Origin createOrigin(String id, String version, String reference) {
        return new Origin(id, version, null, null, Instant.now(), null, Map.of("url", reference));
    }
}
