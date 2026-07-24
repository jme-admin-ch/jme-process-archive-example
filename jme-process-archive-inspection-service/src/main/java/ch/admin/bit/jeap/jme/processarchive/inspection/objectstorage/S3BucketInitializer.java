package ch.admin.bit.jeap.jme.processarchive.inspection.objectstorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jeap.s3.bucket-management.enabled", havingValue = "true")
public class S3BucketInitializer {

    private final S3BucketProperties buckets;
    private final S3BucketService s3;

    @EventListener(classes = ApplicationReadyEvent.class)
    public void initS3StorageBuckets() {
        try {
            log.info("Initializing S3 buckets...");
            initBucket(buckets.getProcessarchiveBucket(), true);
            log.info("All S3 buckets initialized.");
        } catch (S3Exception | IllegalStateException | SdkClientException e) {
            log.error("Failed to initialize S3 buckets.", e);
        }
    }

    private void initBucket(String bucketName, Boolean versioning) {
        log.info("Initializing bucket {} ...", bucketName);
        if (!s3.isBucketExistent(bucketName)) {
            log.info("bucket {} does not exist. creating it...", bucketName);
            s3.createBucket(bucketName);
            // Versioning: so deletes are only soft deletes and overrides keep previous version
            if (versioning) {
                s3.enableVersioning(bucketName);
            }
        } else {
            log.info("Bucket {} already exists.", bucketName);
        }
        log.info("Done initializing bucket {}", bucketName);
    }
}
