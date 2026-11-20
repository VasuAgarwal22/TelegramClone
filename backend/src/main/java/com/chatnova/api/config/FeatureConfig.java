package com.chatnova.api.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Configuration
@EnableScheduling
public class FeatureConfig {
    @Bean
    public VaultCipher vaultCipher(@Value("${chatnova.vault.encryption-key:chatnova-development-key-change-me}") String configuredKey) { return new VaultCipher(configuredKey); }

    public static final class VaultCipher {
        private final SecretKey key;
        private final SecureRandom random = new SecureRandom();
        public VaultCipher(String configuredKey) {
            try { this.key = new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(configuredKey.getBytes(StandardCharsets.UTF_8)), "AES"); }
            catch (Exception exception) { throw new IllegalStateException("Unable to initialize vault encryption", exception); }
        }
        public String encrypt(String plainText) {
            try { byte[] iv = new byte[12]; random.nextBytes(iv); Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv)); byte[] encrypted = cipher.doFinal((plainText == null ? "" : plainText).getBytes(StandardCharsets.UTF_8)); byte[] payload = new byte[iv.length + encrypted.length]; System.arraycopy(iv, 0, payload, 0, iv.length); System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length); return Base64.getEncoder().encodeToString(payload); }
            catch (Exception exception) { throw new IllegalStateException("Unable to encrypt vault item", exception); }
        }
        public String decrypt(String payload) {
            try { byte[] data = Base64.getDecoder().decode(payload); byte[] iv = java.util.Arrays.copyOfRange(data, 0, 12); byte[] encrypted = java.util.Arrays.copyOfRange(data, 12, data.length); Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv)); return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8); }
            catch (Exception exception) { throw new IllegalStateException("Unable to decrypt vault item", exception); }
        }
    }

    @Component
    public static class CurrentUser {
        @Value("${chatnova.current-user-id:user-vasu}") private String userId;
        public String id() { return userId; }
    }
}
