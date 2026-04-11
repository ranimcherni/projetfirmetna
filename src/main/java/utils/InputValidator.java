package utils;

import java.util.regex.Pattern;

public class InputValidator {

    // Regex: Letters, accents, spaces, hyphens, apostrophes. Length 2-50.
    private static final String NAME_REGEX = "^[a-zA-ZÀ-ÿ\\s'-]{2,50}$";
    
    // Standard Email Regex
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Valide le nom ou prénom (lettres uniquement, 2-50 caractères)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return Pattern.matches(NAME_REGEX, name.trim());
    }

    /**
     * Valide le format de l'e-mail
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return Pattern.matches(EMAIL_REGEX, email.trim()) && email.length() <= 254;
    }

    /**
     * Valide le mot de passe (min 8 car, Maj, Min, Chiffre, Caractère spécial)
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    /**
     * Valide un numéro Tunisien (+216 ou local, commence par 2, 5, 7, 9)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        
        // Nettoyage : garder uniquement les chiffres et le signe +
        String cleaned = phone.replaceAll("[\\s-]", "");
        
        // Format : +216 [2579]xxxxxxx ou [2579]xxxxxxx
        if (cleaned.startsWith("+216")) {
            return cleaned.matches("^\\+216[2579]\\d{7}$");
        } else {
            return cleaned.matches("^[2579]\\d{7}$");
        }
    }

    /**
     * Nettoie le numéro de téléphone pour le stockage (enlève espaces et tirets)
     */
    public static String cleanPhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[\\s-]", "");
    }
}
