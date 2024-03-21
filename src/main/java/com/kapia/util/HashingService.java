package com.kapia.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashingService.class);

    public String hash(String str) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes());
            return new String(Hex.encode(messageDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Error hashing string: " + e.getMessage());
            return "ERROR_HASHING_STRING";
        }

    }

    public String hashKey(String str) {
        return str.substring(0, 2) + hash(str);
    }

}
