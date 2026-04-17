package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailingService {

    // Identifiants configurés pour l'envoi
    private static final String SENDER_EMAIL = "ranimcherni03@gmail.com";
    private static final String APP_PASSWORD = "obwqizljfrfbkcsn";

    public static void sendVerificationCode(String recipientEmail, String code) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Code de vérification Firmetna");
        
        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px; max-width: 500px;'>"
                + "<h2 style='color: #2d5a27;'>Récupération de mot de passe</h2>"
                + "<p>Bonjour,</p>"
                + "<p>Vous avez demandé la réinitialisation de votre mot de passe pour votre compte Firmetna.</p>"
                + "<p>Voici votre code de vérification (valide 10 minutes) :</p>"
                + "<h1 style='color: #2d5a27; background: #f4f4f4; padding: 10px; text-align: center; letter-spacing: 5px;'>" + code + "</h1>"
                + "<p>Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet e-mail.</p>"
                + "<p>Cordialement,<br>L'équipe Firmetna</p>"
                + "</div>";

        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
