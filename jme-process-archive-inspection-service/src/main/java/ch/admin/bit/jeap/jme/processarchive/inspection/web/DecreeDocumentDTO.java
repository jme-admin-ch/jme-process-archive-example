package ch.admin.bit.jeap.jme.processarchive.inspection.web;

import java.time.ZonedDateTime;

public record DecreeDocumentDTO(
        String documentId,
        String decreeReferenceId,
        ZonedDateTime createdAt) {
}
