package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import jakarta.validation.constraints.NotNull;

public record NewDecreeDTO(@NotNull String title, @NotNull String someDecreeData) {

}
