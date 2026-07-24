package ch.admin.bit.jeap.jme.processarchive.resource.web;

import ch.admin.bit.jeap.jme.processarchive.resource.domain.DiagramDTO;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.DiagramRepository;
import ch.admin.bit.jeap.jme.processarchive.resource.domain.MessagePublisher;
import ch.admin.bit.jeap.processarchive.test.diagram.v1.Diagram;
import ch.admin.bit.jeap.processarchive.web.AvroWebConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "Diagram", description = "Manage diagrams.")
@RestController
@RequestMapping("/api/diagrams")
@RequiredArgsConstructor
public class DiagramController {
    private static final String ARCHIVE_DATA_SYSTEM_HEADER = "archive-data-system";
    private static final String ARCHIVE_DATA_SCHEMA_HEADER = "archive-data-schema";
    private static final String DIAGRAM_SCHEMA = "Diagram";
    private static final String ARCHIVE_DATA_SCHEMA_VERSION_HEADER = "archive-data-schema-version";
    private static final String DIAGRAM_SCHEMA_VERSION = "1";

    private final DiagramRepository diagramRepository;
    private final MessagePublisher messagePublisher;

    @PutMapping("/{id}")
    @Operation(summary = "Create or update a diagram.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public DiagramDTO saveDiagram(@PathVariable("id") String id, @RequestBody DiagramDTO diagram) {
        if (diagram.getId() == null) {
            diagram.setId(id);
        }
        diagram = diagramRepository.saveDiagram(diagram);
        messagePublisher.diagramVersionCreated(diagram.getId(), diagram.getVersion(), createNewProcessId());
        return diagram;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a diagram.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public DiagramDTO getDiagram(@PathVariable("id") String id, @RequestParam("version") Optional<Integer> version) {
        return version.isPresent() ? diagramRepository.getDiagram(id, version.get()) : diagramRepository.getDiagram(id);
    }

    @GetMapping(value = "/{id}/archival", produces = AvroWebConstants.AVRO_BINARY)
    @Operation(summary = "Get a diagram for archival storage.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public Diagram getArchivalDiagram(@PathVariable("id") String id, @RequestParam("version") int version, HttpServletResponse response) {
        response.addHeader(ARCHIVE_DATA_SYSTEM_HEADER, "JME");
        response.addHeader(ARCHIVE_DATA_SCHEMA_HEADER, DIAGRAM_SCHEMA);
        response.addHeader(ARCHIVE_DATA_SCHEMA_VERSION_HEADER, DIAGRAM_SCHEMA_VERSION);
        DiagramDTO dto = diagramRepository.getDiagram(id, version);
        return Diagram.newBuilder()
                .setId(dto.getId())
                .setName(dto.getName())
                .setVersion(dto.getVersion())
                .setGraph(dto.getGraph())
                .build();
    }

    // Typically, the creation of a new diagram version would be embedded in a business process. But this example does
    // not set up a process, instead we just create a new process id here for simplicity.
    // See the jme-process-context-example project for an example for modelling a process and tracking its progress.
    private String createNewProcessId() {
        return UUID.randomUUID().toString();
    }

}
