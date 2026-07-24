package ch.admin.bit.jeap.jme.processarchive.inspection;

import ch.admin.bit.jeap.jme.processarchive.inspection.objectstorage.S3ObjectStorageConnectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(S3ObjectStorageConnectionProperties.class)
@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
