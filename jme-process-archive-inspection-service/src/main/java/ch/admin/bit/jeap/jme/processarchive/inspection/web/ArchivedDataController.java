package ch.admin.bit.jeap.jme.processarchive.inspection.web;

import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.jme.processarchive.inspection.objectstorage.S3ObjectStorageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * With this very simple controller the data archived by this example's process archive service can be inspected.
 */
@Tag(name = "ArchivedData", description = "Provide archived data.")
@RestController
@RequestMapping("/api/archived-data")
@RequiredArgsConstructor
class ArchivedDataController {

    private final S3ObjectStorageRepository s3ObjectStorageRepository;

    private final Optional<KeyReferenceCryptoService> optionalKeyReferenceCryptoService;

    /*
    PathPatternParser replaced AntPathMatcher as default mvc pathmatch matching strategy starting with Spring Boot 2.6. Contrary
    to the AntPathMatcher, the PathPatternParser only supports "**" for multi-segment matching at the end of a pattern. But as a
    bonus the PathPatternParser supports the additional syntax /{*segments} that allows capturing 0 or more path segments at the
    end. This comes in handy for this controller as archived object keys may contain one or more slashes. Surprisingly, a match
    for /{*segments} results in the 'segments' path variable containing the slash in front of the pattern, i.e. to get the archived
    object key the leading slash needs to be removed from the 'segments' path variable.
    */

    // Essentially, both archived object types (Decree, DecreeDocument) are just text documents. DecreeDocument only pretends
    // to be a pdf file, but in reality is just the byte[] of the String built by concatenating the Decree data.
    @GetMapping(value = "/payload/{bucket}/{*key}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Provide the payload of archived data.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public byte[] getArchivedDataPayload(@PathVariable("bucket") String bucket, @PathVariable("key") String key, @RequestParam("version") Optional<String> version) {
        return s3ObjectStorageRepository.getObject(bucket, removeLeadingSlash(key), version);
    }

    @GetMapping(value = "/payload-string/{bucket}/{*key}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Provide the unencrypted payload of archived data.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public String getArchivedDataPayloadAsString(@PathVariable("bucket") String bucket, @PathVariable("key") String key, @RequestParam("version") Optional<String> version) {
        return optionalKeyReferenceCryptoService.map(keyReferenceCryptoService -> {
            final byte[] decryptedText = keyReferenceCryptoService.decrypt(s3ObjectStorageRepository.getObject(bucket, removeLeadingSlash(key), version));
            return new String(decryptedText, UTF_8);
        }).orElse(s3ObjectStorageRepository.getObjectAsString(bucket, removeLeadingSlash(key), version));
    }

    @GetMapping(value = "/metadata/{bucket}/{*key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Provide the metadata of archived data.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public Map<String, String> getArchivedDataMetadata(@PathVariable("bucket") String bucket, @PathVariable("key") String key, @RequestParam("version") Optional<String> version) {
        return s3ObjectStorageRepository.getMetadata(bucket, removeLeadingSlash(key), version);
    }

    @GetMapping(value = "/retention/{bucket}/{*key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Provide the retention of archived data.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public Map<String, String> getArchivedDataRetention(@PathVariable("bucket") String bucket, @PathVariable("key") String key, @RequestParam("version") Optional<String> version) {
        return s3ObjectStorageRepository.getRetention(bucket, removeLeadingSlash(key), version);
    }

    @GetMapping(value = "/tags/{bucket}/{*key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Provide the tags of archived data.", responses = @ApiResponse(responseCode = "200", description = "success"))
    public Map<String, String> getArchivedDataTags(@PathVariable("bucket") String bucket, @PathVariable("key") String key, @RequestParam("version") Optional<String> version) {
        return s3ObjectStorageRepository.getTags(bucket, removeLeadingSlash(key), version);
    }

    @GetMapping(value = "/{bucket}/lifecycle/configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Provide the lifecycle configuration of a bucket", responses = @ApiResponse(responseCode = "200", description = "success"))
    public List<LifecycleRuleDTO> getLifecycleConfiguration(@PathVariable("bucket") String bucket) {
        return s3ObjectStorageRepository.getLifecycleConfiguration(bucket).stream()
                .map(rule -> new LifecycleRuleDTO(rule.id(),
                        rule.statusAsString(),
                        rule.filter().toString(),
                        (rule.noncurrentVersionExpiration() != null ? rule.noncurrentVersionExpiration().noncurrentDays() : -1),
                        (rule.expiration() != null ? rule.expiration().days() : -1))
                ).toList();
    }

    @GetMapping(value = "/{bucket}/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Provide the list of metadata for objects stored in this bucket during the current day", responses = @ApiResponse(responseCode = "200", description = "success"))
    public List<Map<String, String>> listObjectsFromToday(@PathVariable("bucket") String bucket) {
        return s3ObjectStorageRepository.listObjectsFromToday(bucket);
    }

    private String removeLeadingSlash(String pathSegments) {
        return pathSegments.startsWith("/") ? pathSegments.substring(1) : pathSegments;
    }

}

