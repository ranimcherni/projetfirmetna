package services;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Ce service gère l'authentification à deux facteurs (MFA) via l'algorithme TOTP.
 * Il permet de générer des secrets sécurisés et de valider les codes à 6 chiffres des utilisateurs.
 */
public class GoogleAuthService {

    /** Taille du secret cryptographique fixée à 160 bits pour une sécurité maximale. */
    private static final int SECRET_SIZE = 20; // 160 bits
    /** Nombre de chiffres requis pour le code de validation (standard à 6 chiffres). */
    private static final int CODE_DIGITS = 6;
    /** Intervalle de temps de 30 secondes pour la validité de chaque code généré. */
    private static final int TIME_STEP = 30; // 30 seconds

    /**
     * Cette fonction génère une clé secrète aléatoire de 160 bits pour l'utilisateur.
     * Elle convertit ensuite cette clé en format Base32 pour qu'elle soit lisible par l'humain et les applications.
     */
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * Cette fonction vérifie si le code à 6 chiffres saisi par l'utilisateur est valide.
     * Elle compare le code avec les valeurs attendues sur une plage de 5 minutes pour tolérer les décalages horaires.
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
     * Cette fonction génère l'URL spécifique nécessaire pour créer le QR Code.
     * Ce format standard permet à Google Authenticator d'importer automatiquement le compte et le secret.
     */
    public String getOtpAuthURL(String userEmail, String secretKey) {
        return String.format("otpauth://totp/Firmetna:%s?secret=%s&issuer=Firmetna", userEmail, secretKey);
    }

    /**
     * Cette fonction effectue le calcul mathématique HMAC-SHA1 pour transformer le secret et le temps en code.
     * Elle applique ensuite un tronçonnage binaire pour obtenir exactement les 6 chiffres requis par le standard.
     */
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

    /**
     * Cette fonction convertit des données binaires brutes en une chaîne de texte Base32 sécurisée.
     * Elle est indispensable pour transformer le secret cryptographique en un texte affichable à l'écran.
     */
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

    /**
     * Cette fonction transforme une chaîne Base32 en données binaires compréhensibles par l'ordinateur.
     * Elle permet à l'algorithme de calcul de traiter la clé secrète qui a été stockée sous forme de texte.
     */
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
