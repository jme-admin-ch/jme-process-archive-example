package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DecreeDocumentRepository {

    private final Map<String, DecreeDocument> decreeDocuments = new ConcurrentHashMap<>();

    public void saveDecreeDocument(DecreeDocument decreeDocument) {
        decreeDocuments.put(decreeDocument.getId(), decreeDocument);
    }

    public DecreeDocument getDecreeDocument(String id) {
        return decreeDocuments.get(id);
    }

}
