package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeclarationService {

    private final MessagePublisher messagePublisher;

    public DeclarationDTO createDeclaration(String declarationId, String payload) {
        String processId = UUID.randomUUID().toString();
        messagePublisher.createDeclaration(declarationId, processId, payload);
        return new DeclarationDTO(declarationId, processId);
    }
}
