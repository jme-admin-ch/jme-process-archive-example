package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import jakarta.validation.constraints.NotNull;

public record DecreeDTO(@NotNull String decreeId, @NotNull String decreeDocumentId) {

}
