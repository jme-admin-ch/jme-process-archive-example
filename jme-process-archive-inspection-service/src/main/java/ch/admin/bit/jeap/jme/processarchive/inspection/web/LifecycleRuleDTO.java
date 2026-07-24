package ch.admin.bit.jeap.jme.processarchive.inspection.web;

public record LifecycleRuleDTO(
        String id,
        String status,
        String filter,
        Integer nonCurrentVersionExpirationDays,
        Integer expirationDays) {
}
