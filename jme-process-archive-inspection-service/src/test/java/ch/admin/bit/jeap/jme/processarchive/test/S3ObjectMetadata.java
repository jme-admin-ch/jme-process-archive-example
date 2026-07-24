package ch.admin.bit.jeap.jme.processarchive.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class S3ObjectMetadata {

    @JsonProperty("key-name")
    String keyName;
    @JsonProperty("reference-id")
    String referenceId;
    @JsonProperty("schema")
    String schema;
}
