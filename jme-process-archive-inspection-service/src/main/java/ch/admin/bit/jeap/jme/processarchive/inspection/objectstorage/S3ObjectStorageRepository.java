package ch.admin.bit.jeap.jme.processarchive.inspection.objectstorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Repository
@RequiredArgsConstructor
public class S3ObjectStorageRepository {

    private final S3Client s3Client;

    public byte[] getObject(String bucketName, String objectKey, Optional<String> objectVersionId) {
        return s3Client.getObjectAsBytes(createGetObjectRequest(bucketName, objectKey, objectVersionId)).asByteArray();
    }

    public String getObjectAsString(String bucketName, String objectKey, Optional<String> objectVersionId) {
        return new String(getObject(bucketName, objectKey, objectVersionId), UTF_8);
    }

    private GetObjectRequest createGetObjectRequest(String bucketName, String objectKey, Optional<String> objectVersionId) {
        if (objectVersionId.isPresent()) {
            log.debug("Getting object with key '{}' and version id '{}' from bucket '{}'.", objectKey, objectVersionId.get(), bucketName);
            return GetObjectRequest.builder().bucket(bucketName).key(objectKey).versionId(objectVersionId.get()).build();
        } else {
            log.debug("Getting object with key '{}' from bucket '{}'.", objectKey, bucketName);
           return GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        }
    }

    public Map<String, String> getMetadata(String bucketName, String objectKey, Optional<String> objectVersionId) {
        try {
            return s3Client.headObject(createGetObjectMetadataRequest(bucketName, objectKey, objectVersionId)).metadata();
        } catch (SdkClientException e) {
            throw S3ObjectStorageException.gettingObjectMetadataFailed(e, bucketName, objectKey, objectVersionId);
        }
    }

    public List<Map<String, String>> listObjectsFromToday(String bucketName) {
        List<Map<String, String>> result = new ArrayList<>();

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .build();

        s3Client.listObjectsV2Paginator(listObjectsV2Request).stream()
                .flatMap(r -> r.contents().stream())
                .forEach(s3Object ->
                {
                    Map<String, String> metadata = getMetadata(bucketName, s3Object.key(), Optional.empty());
                    HashMap<String, String> shallowCopy = new HashMap<>(metadata);
                    shallowCopy.put("key-name", s3Object.key());
                    result.add(shallowCopy);
                });
        return result;
    }

    private HeadObjectRequest createGetObjectMetadataRequest(String bucketName, String objectKey, Optional<String> objectVersionId) {
        if (objectVersionId.isPresent()) {
            log.debug("Getting metadata for object with key '{}' and version id '{}' from bucket '{}'.", objectKey, objectVersionId.get(), bucketName);
            return HeadObjectRequest.builder().bucket(bucketName).key(objectKey).versionId(objectVersionId.get()).build();
        } else {
            log.debug("Getting metadata for object with key '{}' from bucket '{}'.", objectKey, bucketName);
            return HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        }
    }

    public Map<String, String> getRetention(String bucketName, String objectKey, Optional<String> objectVersionId) {
        try {
            ObjectLockRetention retention = s3Client.getObjectRetention(createGetObjectRetentionRequest(bucketName, objectKey, objectVersionId)).retention();
            return Map.of("mode", retention.modeAsString(), "retainUntil", retention.retainUntilDate().toString());
        } catch (NoSuchKeyException ase3) {
                return Map.of();
        } catch (SdkClientException e) {
            throw S3ObjectStorageException.gettingObjectRetentionFailed(e, bucketName, objectKey, objectVersionId);
        }
    }

    private GetObjectRetentionRequest createGetObjectRetentionRequest(String bucketName, String objectKey, Optional<String> objectVersionId) {
        if (objectVersionId.isPresent()) {
            log.debug("Getting retention for object with key '{}' and version id '{}' from bucket '{}'.", objectKey, objectVersionId.get(), bucketName);
            return GetObjectRetentionRequest.builder().bucket(bucketName).key(objectKey).versionId(objectVersionId.get()).build();
        } else {
            log.debug("Getting retention for object with key '{}' from bucket '{}'.", objectKey, bucketName);
            return GetObjectRetentionRequest.builder().bucket(bucketName).key(objectKey).build();
        }
    }

    public Map<String, String> getTags(String bucketName, String objectKey, Optional<String> objectVersionId) {
        try {
            GetObjectTaggingResponse response = s3Client.getObjectTagging(createGetObjectTaggingRequest(bucketName, objectKey, objectVersionId));
            return response.tagSet().stream()
                    .collect(toMap(Tag::key, Tag::value, (first, second) -> first, TreeMap::new));
        } catch (SdkClientException e) {
            throw S3ObjectStorageException.gettingObjectTagsFailed(e, bucketName, objectKey, objectVersionId);
        }
    }

    private GetObjectTaggingRequest createGetObjectTaggingRequest(String bucketName, String objectKey, Optional<String> objectVersionId) {
        if (objectVersionId.isPresent()) {
            log.debug("Getting tags for object with key '{}' and version id '{}' from bucket '{}'.", objectKey, objectVersionId.get(), bucketName);
            return GetObjectTaggingRequest.builder().bucket(bucketName).key(objectKey).versionId(objectVersionId.get()).build();
        } else {
            log.debug("Getting tags for object with key '{}' from bucket '{}'.", objectKey, bucketName);
            return GetObjectTaggingRequest.builder().bucket(bucketName).key(objectKey).build();
        }
    }

    public List<LifecycleRule> getLifecycleConfiguration(String bucket) {
        return s3Client.getBucketLifecycleConfiguration(GetBucketLifecycleConfigurationRequest.builder().bucket(bucket).build()).rules();
    }
}
