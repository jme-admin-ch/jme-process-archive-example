package ch.admin.bit.jeap.jme.processarchive.resource.kafka;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Configuration
@ConfigurationProperties(prefix = "jme.processarchive.topic")
@Data
@Slf4j
class TopicConfiguration {

    private String decreeCreated;
    private String decreeDocumentCreated;
    private String diagramVersionCreated;

    @Configuration
    @Profile("!local")
    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    private static class TopicConfigurationCloud {

        private final KafkaAdmin kafkaAdmin;
        private final TopicConfiguration topicConfiguration;

        @PostConstruct
        public void checkIfTopicsExist() throws ExecutionException, InterruptedException {
            try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                List<String> topicNames = List.of(
                        topicConfiguration.getDecreeCreated(),
                        topicConfiguration.getDecreeDocumentCreated(),
                        topicConfiguration.getDiagramVersionCreated()
                );

                for (String topicName : topicNames) {
                    try {
                        adminClient.describeTopics(Set.of(topicName)).allTopicNames().get();
                    } catch (Exception ex) {
                        log.error("Unable to access topic " + topicName, ex);
                        throw ex;
                    }
                }
            }
        }
    }

    @Configuration
    @Profile("local")
    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    private static class TopicConfigurationLocal {

        private final TopicConfiguration topicConfiguration;

        @Bean
        public NewTopic decreeCreatedTopic() {
            return new NewTopic(topicConfiguration.getDecreeCreated(), 10, (short) 1);
        }

        @Bean
        public NewTopic decreeDocumentCreatedTopic() {
            return new NewTopic(topicConfiguration.getDecreeDocumentCreated(), 10, (short) 1);
        }

        @Bean
        public NewTopic diagramVersionCreatedTopic() {
            return new NewTopic(topicConfiguration.getDiagramVersionCreated(), 10, (short) 1);
        }

    }

}
