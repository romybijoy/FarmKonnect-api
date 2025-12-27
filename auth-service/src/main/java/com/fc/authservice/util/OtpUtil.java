package com.fc.authservice.util;

import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility class for generating secure One-Time Passwords (OTPs).
 * Uses a cryptographically strong implementation of SecureRandom
 * to generate a 6-digit numeric OTP.
 */
@Component
public class OtpUtil {

    /** Cryptographically secure random number generator */
    private final Random random;

    /**
     * Initializes the SecureRandom instance.
     * If no strong algorithm is available, the application fails fast.
     */
    public OtpUtil() {
        try {
            this.random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Strong instance of SecureRandom is not available", e);
        }
    }

    /**
     * Generates a 6-digit OTP as a string.
     * The OTP may include leading zeros.
     *
     * @return a secure 6-digit OTP
     */
    public String generateOtp() {
        // Generate a number between 0 and 999999
        int randomNumber = this.random.nextInt(999999);

        StringBuilder output = new StringBuilder(Integer.toString(randomNumber));

        // Format to always produce exactly 6 digits (with leading zeros if necessary)
        while (output.length() < 6) {
            output.insert(0, "0");
        }
        return output.toString();
    }
}
