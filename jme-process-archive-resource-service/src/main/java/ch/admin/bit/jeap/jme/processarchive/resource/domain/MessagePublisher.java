package ch.admin.bit.jeap.jme.processarchive.resource.domain;

public interface MessagePublisher {

    void decreeCreated(Decree decree, String processId);

    void decreeDocumentCreated(String decreeDocumentId, String processId);

    void diagramVersionCreated(String diagramId, int diagramVersion, String processId);

    void createDeclaration(String declarationId, String processId, String payload);

}
