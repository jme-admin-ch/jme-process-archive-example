package ch.admin.bit.jeap.jme.processarchive.resource.web;

import ch.admin.bit.jeap.jme.processarchive.resource.domain.DecreeDocumentRepository;
import ch.admin.bit.jeap.processarchive.test.DecreeReference;
import ch.admin.bit.jeap.processarchive.test.decreedocument.v1.DecreeDocument;
import ch.admin.bit.jeap.processarchive.web.AvroWebConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.ByteBuffer;

/**
 * This does not need to be a separate controller. Archival decree documents could also be provided by the
 * DecreeController itself e.g. at the path /api/decree/{id}/archival
 */
@Tag(name = "Archive", description = "Provide archive data.")
@RestController
@RequestMapping("/api/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private static final String ARCHIVE_DATA_SYSTEM_HEADER = "archive-data-system";
    private static final String ARCHIVE_DATA_SCHEMA_HEADER = "archive-data-schema";
    private static final String DECREE_DOCUMENT_SCHEMA = "DecreeDocument";
    private static final String ARCHIVE_DATA_SCHEMA_VERSION_HEADER = "archive-data-schema-version";
    private static final int DECREE_DOCUMENT_SCHEMA_VERSION = 1;
    private static final String ARCHIVE_METADATA_HEADER_PREFIX = "archive-metadata-";

    private final DecreeDocumentRepository decreeDocumentRepository;

    /**
     * The returned {@link DecreeDocument} (an avro-generated class) will be transparently serialized to binary avro
     * by jeap-process-archive-web as long as the produced media type is specified as "avro/binary".
     */
    @GetMapping(value = "/decreedocuments/{id}", produces = AvroWebConstants.AVRO_BINARY)
    @Operation(summary = "Provide decree document archive data.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public DecreeDocument getArchivalDecreeDocument(@PathVariable("id") String id, HttpServletResponse response) {
        response.addHeader(ARCHIVE_DATA_SYSTEM_HEADER, "JME");
        response.addHeader(ARCHIVE_DATA_SCHEMA_HEADER, DECREE_DOCUMENT_SCHEMA);
        response.addHeader(ARCHIVE_DATA_SCHEMA_VERSION_HEADER, String.valueOf(DECREE_DOCUMENT_SCHEMA_VERSION));
        response.addHeader(ARCHIVE_METADATA_HEADER_PREFIX + "issuer", "John Smith");
        ch.admin.bit.jeap.jme.processarchive.resource.domain.DecreeDocument domainDocument = decreeDocumentRepository.getDecreeDocument(id);

        if (domainDocument == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Decree document with id " + id + " not found");
        }

        return DecreeDocument.newBuilder()
                .setDecreeReferenceBuilder(DecreeReference.newBuilder()
                        .setId(domainDocument.getDecreeId())
                        .setType("Example"))
                .setCreatedAt(domainDocument.getCreatedAt().toInstant())
                .setPdf(ByteBuffer.wrap(domainDocument.getPdf()))
                .setDocumentId(domainDocument.getId())
                .build();
    }
}
