package com.elif.mcpproject.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MessageCryptoService {
    private static final String PREFIX = "enc:v1:";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec key;

    public MessageCryptoService(@Value("${app.message-encryption-key}") String encryptionKey) {
        this.key = new SecretKeySpec(sha256(encryptionKey), "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.startsWith(PREFIX)) {
            return plainText;
        }

        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Message encryption failed", e);
        }
    }

    public String decrypt(String storedText) {
        if (storedText == null || !storedText.startsWith(PREFIX)) {
            return storedText;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(storedText.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);

            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Mesaj cozumlenemedi.";
        }
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Message encryption key could not be initialized", e);
        }
    }
}
