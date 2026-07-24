package ch.admin.bit.jeap.jme.processarchive.inspection.web;

import ch.admin.bit.jeap.processarchive.reader.ProcessArchiveReader;
import ch.admin.bit.jeap.processarchive.test.decreedocument.v1.DecreeDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Optional;

/**
 * This simple controller shows the use of the ProcessArchiveReader, which reads an object from a bucket and returns the instantiated object with the reader schema
 */
@Tag(name = "DecreeDocument", description = "Provide DecreeDocument data.")
@RestController
@RequestMapping("/api/decree-document")
@RequiredArgsConstructor
class DecreeDocumentController {

    private final ProcessArchiveReader archiveReader;

    @GetMapping(value = "/{bucket}/{*key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Provide the archived decreeDocument.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public DecreeDocumentDTO getArchivedDecreeDocument(@PathVariable("bucket") String bucket, @PathVariable("key") String key, @RequestParam("version") Optional<String> version) {
        final DecreeDocument decreeDocument = archiveReader.readArtifact(DecreeDocument.class, bucket, key, version.orElse(null));
        return new DecreeDocumentDTO(decreeDocument.getDocumentId(), decreeDocument.getDecreeReference().getId(), decreeDocument.getCreatedAt().atZone(ZoneId.systemDefault()));
    }

}

