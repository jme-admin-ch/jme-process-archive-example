package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
public class DecreeDocument {

    private final String id = UUID.randomUUID().toString();

    private final String decreeId;

    private final ZonedDateTime createdAt = ZonedDateTime.now();

    @ToString.Exclude
    private final byte[] pdf;
}
