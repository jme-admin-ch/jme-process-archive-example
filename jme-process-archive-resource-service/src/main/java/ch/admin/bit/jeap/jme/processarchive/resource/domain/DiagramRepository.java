package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DiagramRepository {

    private Map<String, List<DiagramDTO>> diagrams = new HashMap<>();

    public synchronized DiagramDTO saveDiagram(DiagramDTO diagram) {
        List<DiagramDTO> versions = diagrams.computeIfAbsent(diagram.getId(), k -> new ArrayList<>());
        diagram.setVersion(versions.size() + 1);
        versions.add(diagram);
        return diagram;
    }

    public DiagramDTO getDiagram(String id, int version) {
        return diagrams.get(id).get(version-1);
    }

    public DiagramDTO getDiagram(String id) {
        List<DiagramDTO> versions = diagrams.get(id);
        return getDiagram(id, versions.size());
    }

}
