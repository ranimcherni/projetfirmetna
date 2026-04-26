package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    /**
     * Génère un QR Code sous forme de WritableImage (JavaFX) à partir d'un texte.
     *
     * @param text   Le texte à encoder (ex: détails de l'événement).
     * @param width  La largeur de l'image souhaitée.
     * @param height La hauteur de l'image souhaitée.
     * @return L'image JavaFX contenant le QR Code.
     */
    public static WritableImage generateQRCodeImage(String text, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // Encodage en UTF-8 pour supporter les accents (très important pour les descriptions)
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // Niveau de correction d'erreur moyen pour supporter plus de données
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // Marge autour du QR code
        hints.put(EncodeHintType.MARGIN, 1);

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            WritableImage image = new WritableImage(width, height);
            
            // Parcours de la matrice pour dessiner l'image pixel par pixel
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Les modules remplis sont noirs, le fond est blanc (transparent optionnel)
                    Color color = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                    image.getPixelWriter().setColor(x, y, color);
                }
            }
            return image;
        } catch (WriterException e) {
            System.err.println("Erreur lors de la génération du QR Code : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
