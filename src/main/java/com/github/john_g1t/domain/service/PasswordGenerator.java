package com.github.john_g1t.domain.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public final class PasswordGenerator {
    private static PasswordGenerator INSTANCE;
    private final byte[] salt;

    private PasswordGenerator(String salt) {
        this.salt = salt.getBytes(StandardCharsets.UTF_8);
    }

    public static PasswordGenerator getInstance() {
        if (INSTANCE == null) {
            throw new AssertionError("You have to call init first");
        }

        return INSTANCE;
    }

    public static PasswordGenerator init(String salt) {
        if (INSTANCE != null) {
            throw new AssertionError("Already initialized");
        }

        INSTANCE = new PasswordGenerator(salt);
        return INSTANCE;
    }

    public String hash(String password){
        KeySpec spec = new PBEKeySpec(password.toCharArray(), this.salt, 65536, 128);
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Wrong hashing algorithm");
            System.exit(1);
            return "";
        }

        byte[] hash;
        try {
            hash = factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            System.out.println("Wrong hashing parameters");
            System.exit(1);
            return "";
        }

        return new String(hash, StandardCharsets.UTF_8);
    }

    public boolean verify(String password, String validate){
        KeySpec spec = new PBEKeySpec(password.toCharArray(), this.salt, 65536, 128);
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Wrong hashing algorithm");
            System.exit(1);
            return false;
        }

        byte[] hash;
        try {
            hash = factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            System.out.println("Wrong hashing parameters");
            System.exit(1);
            return false;
        }

        return new String(hash, StandardCharsets.UTF_8).equals(validate);
    }
}
