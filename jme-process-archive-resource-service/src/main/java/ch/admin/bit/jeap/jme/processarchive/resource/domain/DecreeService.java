package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class DecreeService {

    private final DecreeRepository decreeRepository;
    private final DecreeDocumentRepository decreeDocumentRepository;
    private final MessagePublisher messagePublisher;

    public DecreeDTO issueDecree(NewDecreeDTO newDecreeDTO, String processId) {
        log.info("Issue decree with processId {}", processId);
        Decree decree = Decree.builder()
                .title(newDecreeDTO.title())
                .someDecreeData(newDecreeDTO.someDecreeData())
                .build();
        decreeRepository.saveDecree(decree);
        log.info("Decree created: " + decree.getId());
        messagePublisher.decreeCreated(decree, processId);

        DecreeDocument decreeDocument = DecreeDocument.builder()
                .decreeId(decree.getId())
                .pdf(createDecreePdf(decree))
                .build();
        decreeDocumentRepository.saveDecreeDocument(decreeDocument);
        messagePublisher.decreeDocumentCreated(decreeDocument.getId(), processId);
        log.info("Decree document created: " + decreeDocument.getId());

        return new DecreeDTO(decree.getId(), decreeDocument.getId());
    }

    private byte[] createDecreePdf(Decree decree) {
        // We don't create a real PDF here, just a dummy byte array.
        String decreeContent = decree.getTitle() + decree.getSomeDecreeData();
        return decreeContent.getBytes(StandardCharsets.UTF_8);
    }
}
