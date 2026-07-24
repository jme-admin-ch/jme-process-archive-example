package ch.admin.bit.jeap.jme.processarchive.inspection.objectstorage;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class S3ObjectStorageException extends RuntimeException {

    public S3ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public static S3ObjectStorageException connectingFailed(Exception e, S3ObjectStorageConnectionProperties s3ObjectStorageConnectionProperties) {
        String msg = String.format("Error connecting to server using configuration %s.", s3ObjectStorageConnectionProperties.toString());
        log.error(msg, e);
        return new S3ObjectStorageException(msg, e);
    }

    public static S3ObjectStorageException checkingIfBucketExistsFailed(Exception e, String bucketName) {
        String msg = String.format("Error checking if bucket '%s' exists.", bucketName);
        log.error(msg, e);
        return new S3ObjectStorageException(msg, e);
    }

    public static S3ObjectStorageException gettingObjectFailed(Exception e, String bucketName, String objectKey) {
        String msg = String.format("Error getting object with key '%s' from bucket '%s'.", objectKey, bucketName);
        log.error(msg, e);
        return new S3ObjectStorageException(msg, e);
    }

    public static S3ObjectStorageException gettingObjectFailed(Exception e, String bucketName, String objectKey, Optional<String> objectVersionId) {
        String msg;
        if (objectVersionId.isPresent()) {
            msg = String.format("Error getting object with key '%s' and version '%s' from bucket '%s'.", objectKey, objectVersionId.get(), bucketName);
        } else {
            msg = String.format("Error getting object with key '%s' from bucket '%s'.", objectKey, bucketName);
        }
        log.error(msg, e);
        return new S3ObjectStorageException(msg, e);
    }

    public static S3ObjectStorageException gettingObjectMetadataFailed(Exception e, String bucketName, String objectKey, Optional<String> objectVersionId) {
        String msg = String.format("Error getting metadata of object with key '%s' and version '%s' from bucket '%s'.", objectKey, objectVersionId, bucketName);
        log.error(msg, e);
        return new S3ObjectStorageException(msg, e);
    }

    public static S3ObjectStorageException gettingObjectRetentionFailed(Exception e, String bucketName, String objectKey, Optional<String> objectVersionId) {
        String msg = String.format("Error getting retention of object with key '%s' and version '%s' from bucket '%s'.", objectKey, objectVersionId, bucketName);
        log.error(msg, e);
        return new S3ObjectStorageException(msg, e);
    }

    public static S3ObjectStorageException gettingObjectTagsFailed(Exception e, String bucketName, String objectKey, Optional<String> objectVersionId) {
        String msg = String.format("Error getting tags of object with key '%s' and version '%s' from bucket '%s'.", objectKey, objectVersionId, bucketName);
        log.error(msg, e);
        return new S3ObjectStorageException(msg, e);
    }

}
