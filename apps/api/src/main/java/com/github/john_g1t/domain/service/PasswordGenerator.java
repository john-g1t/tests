package com.github.john_g1t.domain.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public final class PasswordGenerator {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 128;

    private static PasswordGenerator INSTANCE;
    private final byte[] salt;

    private PasswordGenerator(String salt) {
        this.salt = salt.getBytes(StandardCharsets.UTF_8);
    }

    public static PasswordGenerator getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("You have to call init first");
        }
        return INSTANCE;
    }

    public static PasswordGenerator init(String salt) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already initialized");
        }
        INSTANCE = new PasswordGenerator(salt);
        return INSTANCE;
    }

    public String hash(String password) {
        byte[] hash = generateHash(password);
        return new String(hash, StandardCharsets.UTF_8);
    }

    public boolean verify(String password, String validate) {
        return hash(password).equals(validate);
    }

    private byte[] generateHash(String password) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), this.salt, ITERATIONS, KEY_LENGTH);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm not available: " + ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid hashing parameters", e);
        }
    }
}