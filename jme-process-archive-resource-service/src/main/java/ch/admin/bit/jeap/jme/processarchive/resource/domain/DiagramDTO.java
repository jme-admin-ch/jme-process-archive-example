package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class DiagramDTO {

    private String id;

    private Integer version;

    @NonNull
    private String name;

    @NonNull
    private String graph;

}
