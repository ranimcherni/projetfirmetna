package services;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.nio.IntBuffer;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class FaceAuthService {

    private CascadeClassifier faceDetector;
    private FaceRecognizer faceRecognizer;
    
    private static final String CASCADE_PATH = "src/main/resources/models/haarcascade_frontalface_default.xml";
    private static final String MODEL_PATH = "src/main/resources/models/face_model.yml";

    /**
     * Le constructeur initialise le détecteur Haar Cascade et l'algorithme de reconnaissance LBPH.
     * Il charge également automatiquement le modèle existant depuis le disque s'il a déjà été créé.
     */
    public FaceAuthService() {
        // Validation stricte du modèle Haar Cascade
        File cascadeFile = new File(CASCADE_PATH);
        if (!cascadeFile.exists()) {
            throw new RuntimeException("Erreur critique : Fichier Haar Cascade introuvable à " + cascadeFile.getAbsolutePath());
        }
        
        faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
        faceRecognizer = LBPHFaceRecognizer.create();
        
        // Charger le modèle existant s'il y a déjà des utilisateurs enregistrés
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            faceRecognizer.read(modelFile.getAbsolutePath());
        }
    }

    /**
     * Cette fonction détecte un visage dans une image brute et le convertit en niveaux de gris.
     * Elle redimensionne ensuite le visage en 200x200 pixels pour garantir une analyse uniforme par l'algorithme.
     */
    public Mat extractFace(Mat rawFrame) {
        Mat grayFrame = new Mat();
        cvtColor(rawFrame, grayFrame, COLOR_BGR2GRAY);
        equalizeHist(grayFrame, grayFrame);

        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(grayFrame, faces, 1.1, 5, 0, new Size(100, 100), new Size(1000, 1000));

        if (faces.size() > 0) {
            // Prend le premier visage trouvé
            Rect face = faces.get(0);
            Mat cropped = new Mat(grayFrame, face);
            Mat resized = new Mat();
            resize(cropped, resized, new Size(200, 200));
            return resized;
        }
        return null;
    }

    /**
     * Cette fonction lie une liste d'images capturées à un identifiant d'utilisateur spécifique.
     * Elle entraîne ensuite le modèle et sauvegarde les résultats dans le fichier .yml pour une utilisation future.
     */
    public void trainFaces(int userId, List<Mat> faceImages) {
        if (faceImages == null || faceImages.isEmpty()) return;

        MatVector imagesVector = new MatVector(faceImages.size());
        Mat labels = new Mat(faceImages.size(), 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        for (int i = 0; i < faceImages.size(); i++) {
            imagesVector.put(i, faceImages.get(i));
            labelsBuf.put(i, userId);
        }

        System.out.println("Début de l'entraînement LBPH pour l'utilisateur ID: " + userId + " avec " + faceImages.size() + " images.");
        
        // Si le fichier existe déjà, on le met à jour (update), sinon on l'entraîne (train)
        if (new File(MODEL_PATH).exists()) {
            faceRecognizer.update(imagesVector, labels);
        } else {
            File parentDir = new File(MODEL_PATH).getParentFile();
            if (!parentDir.exists()) parentDir.mkdirs();
            faceRecognizer.train(imagesVector, labels);
        }
        
        faceRecognizer.write(new File(MODEL_PATH).getAbsolutePath());
        System.out.println("Entraînement terminé et modèle sauvegardé !");
    }

    /**
     * Cette fonction compare un visage capturé en direct avec tous les visages enregistrés dans le modèle.
     * Elle retourne l'ID de l'utilisateur si la ressemblance est forte, ou -1 si le visage est inconnu.
     */
    public int recognizeFace(Mat faceImage) {
        if (!new File(MODEL_PATH).exists()) {
            System.out.println("Aucun modèle de reconnaissance facial existant.");
            return -1;
        }

        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);

        try {
            faceRecognizer.predict(faceImage, label, confidence);
            int predictedId = label.get(0);
            double conf = confidence.get(0);
            
            System.out.println("Reconnaissance - ID Prédit: " + predictedId + " | Confiance (plus petite est mieux, lbph d=0..120): " + conf);
            
            // On abaisse le seuil de 75.0 à 50.0 pour être beaucoup plus strict
            // et différencier les visages très similaires (comme deux sœurs)
            if (conf <= 50.0 && predictedId > 0) {
                return predictedId;
            } else {
                return -1; // Inconnu ou douteux
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
