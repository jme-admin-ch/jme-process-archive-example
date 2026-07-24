package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.storage.HashProvider;
import org.bouncycastle.crypto.digests.Blake3Digest;
import org.springframework.stereotype.Component;

import java.util.HexFormat;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class JmeHashProvider implements HashProvider {

    private static final int DEFAULT_DIGEST_LENGTH_IN_BYTES = 32;

    @Override
    public String hashStorageObjectId(String referenceId, String referenceIdType) {
        return hash(referenceId, referenceIdType);
    }

    @Override
    public String hashReferenceId(String referenceId, String referenceIdType) {
        return hash(referenceId, referenceIdType);
    }

    @Override
    public String hashPayload(byte[] payload) {
        final Blake3Digest messageDigest = new Blake3Digest(DEFAULT_DIGEST_LENGTH_IN_BYTES);
        messageDigest.update(payload, 0, payload.length);
        return generateHash(messageDigest);
    }

    private String hash(String... parts) {
        final Blake3Digest messageDigest = new Blake3Digest(DEFAULT_DIGEST_LENGTH_IN_BYTES);
        for (String part : parts) {
            byte[] partBytes = part.getBytes(UTF_8);
            messageDigest.update(partBytes, 0, partBytes.length);
        }
        return generateHash(messageDigest);
    }

    private String generateHash(Blake3Digest messageDigest) {
        final byte[] hashedString = new byte[messageDigest.getDigestSize()];
        messageDigest.doFinal(hashedString, 0);
        return HexFormat.of().formatHex(hashedString);
    }
}
