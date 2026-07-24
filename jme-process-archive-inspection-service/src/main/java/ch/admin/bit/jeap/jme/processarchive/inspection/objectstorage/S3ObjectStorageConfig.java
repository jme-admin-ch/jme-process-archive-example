package ch.admin.bit.jeap.jme.processarchive.inspection.objectstorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.http.urlconnection.ProxyConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Configuration
public class S3ObjectStorageConfig {

    @Bean
    S3Client s3Client(S3ObjectStorageConnectionProperties connectionProperties) {
        S3Client s3Client;

        log.info("Initializing s3Client with connection properties {}.", connectionProperties.toString());

        ClientOverrideConfiguration.Builder overrideConfig = ClientOverrideConfiguration.builder();
        overrideConfig.advancedOptions(Map.of(SdkAdvancedClientOption.SIGNER, AwsS3V4Signer.create()));

        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .region(connectionProperties.getRegion())
                .forcePathStyle(true)
                .credentialsProvider(awsCredentialsProvider(connectionProperties))
                .httpClient(UrlConnectionHttpClient.builder()
                        .proxyConfiguration(ProxyConfiguration.builder()
                                .useSystemPropertyValues(false)
                                .useEnvironmentVariablesValues(false)
                                .build())
                        .build())
                .overrideConfiguration(overrideConfig.build());
        if (hasText(connectionProperties.getAccessUrl())) {
            log.info("Overriding endpoint in S3Client...");
            s3ClientBuilder = s3ClientBuilder.endpointOverride(retrieveEndpointURI(connectionProperties.getAccessUrl()));
        }
        s3Client = s3ClientBuilder.build();

        log.info("The initialization of s3Client was successful");
        return s3Client;
    }

    private AwsCredentialsProvider awsCredentialsProvider(S3ObjectStorageConnectionProperties props) {
        if (props.getAccessKey() != null && props.getSecretKey() != null) {
            log.debug("Creating AwsCredentialsProvider using configured accessKey and secretKey...");
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }

    private URI retrieveEndpointURI(String accessUrl) {
        if (accessUrl.startsWith("http://") || accessUrl.startsWith("https://")) {
            return URI.create(accessUrl);
        }
        return URI.create("https://" + accessUrl);
    }

}
