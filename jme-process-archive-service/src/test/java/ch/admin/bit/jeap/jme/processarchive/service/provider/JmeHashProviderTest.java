package ch.admin.bit.jeap.jme.processarchive.service.provider;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JmeHashProviderTest {

    private final JmeHashProvider hashProvider = new JmeHashProvider();

    @Test
    void hashStorageObjectId() {
        assertThat(hashProvider.hashStorageObjectId("referenceId", "referenceIdType"))
                .isEqualTo("017b56f3d6f105b27e4ed89c0fa3fa7dcc4b3e355d9db66c3b4b626deeb7fb01");
    }

    @Test
    void hashReferenceId() {
        assertThat(hashProvider.hashReferenceId("referenceId", "referenceIdType"))
                .isEqualTo("017b56f3d6f105b27e4ed89c0fa3fa7dcc4b3e355d9db66c3b4b626deeb7fb01");
    }

    @Test
    void hashPayload() {
        assertThat(hashProvider.hashPayload("payload".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("ec90915fa26ab012a89a88ecc8b47e4dd76c4adfd6abd1fc10e321b0fca18d1d");
    }
}
