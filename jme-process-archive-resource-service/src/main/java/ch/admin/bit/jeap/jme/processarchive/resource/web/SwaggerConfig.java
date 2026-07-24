package ch.admin.bit.jeap.jme.processarchive.resource.web;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "JME Process Archive Example Resource",
                description = "An example resource which has its artifacts archived by the process archive service",
                contact = @Contact(
                        email = "jEAP-Community@bit.admin.ch",
                        name = "jEAP",
                        url = "https://confluence.eap.bit.admin.ch/display/BLUE/"
                )
        ),
        externalDocs = @ExternalDocumentation(
                url = "https://confluence.eap.bit.admin.ch/display/JEAP/Blueprint+Microservices",
                description = "Blueprint Microservices in Confluence")
)
@Configuration
public class SwaggerConfig {
}
