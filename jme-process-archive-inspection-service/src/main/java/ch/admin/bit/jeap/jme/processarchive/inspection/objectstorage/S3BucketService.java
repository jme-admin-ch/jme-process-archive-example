package ch.admin.bit.jeap.jme.processarchive.inspection.objectstorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3BucketService {

    private final S3Client s3Client;

    public boolean isBucketExistent(String bucketName) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        try {
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    public void createBucket(String bucketName) {
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        s3Client.createBucket(bucketRequest);
    }

    public void deleteBucket(String bucketName) {
        try {
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.deleteBucket(deleteBucketRequest);
            log.debug("Successfully deleted bucket : {}", bucketName);
        } catch (S3Exception e) {
            log.error(e.getMessage());
        }
    }

    public void enableVersioning(String bucket) {
        try {
            log.debug("enabling versioning for bucket {} ...", bucket);
            var configuration = VersioningConfiguration.builder().status(BucketVersioningStatus.ENABLED).build();

            var request = PutBucketVersioningRequest.builder()
                    .bucket(bucket)
                    .versioningConfiguration(configuration)
                    .build();

            s3Client.putBucketVersioning(request);
        } catch (S3Exception e) {
            throw new IllegalStateException("failed to set cors on bucket", e);
        }
    }

    public List<String> listBuckets() {
        return s3Client.listBuckets().buckets().stream().map(Bucket::name).toList();
    }
}
