package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
public class Decree {

    private final String id = UUID.randomUUID().toString();

    private final ZonedDateTime createdAt = ZonedDateTime.now();

    @NonNull
    private final String title;

    @NonNull
    private final String someDecreeData;
}
