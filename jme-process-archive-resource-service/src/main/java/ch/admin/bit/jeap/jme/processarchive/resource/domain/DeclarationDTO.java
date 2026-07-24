package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import jakarta.validation.constraints.NotNull;

public record DeclarationDTO(@NotNull String declarationId, @NotNull String processId) {
}
