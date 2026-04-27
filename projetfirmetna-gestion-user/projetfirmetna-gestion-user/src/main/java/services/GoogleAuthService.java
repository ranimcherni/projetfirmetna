package services;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Service for Google Authenticator (TOTP) - Pure Java Implementation
 */
public class GoogleAuthService {

    private static final int SECRET_SIZE = 20; // 160 bits
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP = 30; // 30 seconds

    /**
     * Generates a random Base32 secret key.
     */
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * Validates the 6-digit code provided by the user.
     */
    public boolean authorize(String secretKey, int code) {
        if (secretKey == null || secretKey.trim().isEmpty()) return false;
        
        String cleanedKey = secretKey.trim();
        long currentInterval = System.currentTimeMillis() / 1000 / TIME_STEP;
        
        // Check intervals to account for time drift (total 5 min window)
        for (int i = -5; i <= 5; i++) {
            if (calculateCode(cleanedKey, currentInterval + i) == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a URI for the QR Code.
     */
    public String getOtpAuthURL(String userEmail, String secretKey) {
        return String.format("otpauth://totp/Firmetna:%s?secret=%s&issuer=Firmetna", userEmail, secretKey);
    }

    private int calculateCode(String secretKey, long interval) {
        byte[] key = decodeBase32(secretKey);
        byte[] data = ByteBuffer.allocate(8).putLong(interval).array();

        try {
            SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xf;
            int truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xff);
            }

            truncatedHash &= 0x7fffffff;
            truncatedHash %= 1000000;

            return truncatedHash;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error calculating TOTP code", e);
        }
    }

    // --- Base32 Implementation (Pure Java) ---

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private String encodeBase32(byte[] bytes) {
        StringBuilder sb = new StringBuilder((bytes.length + 7) * 8 / 5);
        int bitBuffer = 0;
        int bitCount = 0;
        for (byte b : bytes) {
            bitBuffer = (bitBuffer << 8) | (b & 0xFF);
            bitCount += 8;
            while (bitCount >= 5) {
                sb.append(BASE32_CHARS.charAt((bitBuffer >> (bitCount - 5)) & 0x1F));
                bitCount -= 5;
            }
        }
        if (bitCount > 0) {
            sb.append(BASE32_CHARS.charAt((bitBuffer << (5 - bitCount)) & 0x1F));
        }
        return sb.toString();
    }

    private byte[] decodeBase32(String base32) {
        base32 = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        byte[] bytes = new byte[base32.length() * 5 / 8];
        int bitBuffer = 0;
        int bitCount = 0;
        int byteCount = 0;
        for (char c : base32.toCharArray()) {
            bitBuffer = (bitBuffer << 5) | BASE32_CHARS.indexOf(c);
            bitCount += 5;
            if (bitCount >= 8) {
                bytes[byteCount++] = (byte)(bitBuffer >> (bitCount - 8));
                bitCount -= 8;
            }
        }
        return bytes;
    }
}
