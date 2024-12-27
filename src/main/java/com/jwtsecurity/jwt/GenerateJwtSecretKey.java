package com.jwtsecurity.jwt;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateJwtSecretKey {
    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[64];
        secureRandom.nextBytes(key);
        String secretKey = Base64.getEncoder().encodeToString(key);
        System.out.println("secretKey를 생성합니다. " + secretKey);
    }
}
