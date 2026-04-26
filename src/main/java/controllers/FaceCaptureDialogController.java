package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import services.FaceAuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FaceCaptureDialogController {

    @FXML private ImageView webcamView;
    @FXML private Button captureButton;
    @FXML private Label statusLabel;

    private OpenCVFrameGrabber grabber;
    private JavaFXFrameConverter converter = new JavaFXFrameConverter();
    private ScheduledExecutorService timer;
    
    private FaceAuthService faceAuthService;
    private boolean isEnrollmentMode = false;
    
    // Result handling
    private boolean successful = false;
    private List<Mat> capturedFaces = new ArrayList<>();
    private int recognizedUserId = -1;

    public void initData(boolean isEnrollment) {
        this.isEnrollmentMode = isEnrollment;
        this.faceAuthService = new FaceAuthService();
        captureButton.setText(isEnrollment ? "Enregistrer mon visage" : "Analyser mon visage");
        startCamera();
    }

    private void startCamera() {
        try {
            // Utilise la webcam par défaut (0)
            grabber = new OpenCVFrameGrabber(0);
            grabber.setFrameRate(30);
            grabber.start();

            Runnable frameGrabber = new Runnable() {
                @Override
                public void run() {
                    try {
                        Frame frame = grabber.grab();
                        Image imageToShow = converter.convert(frame);
                        Platform.runLater(() -> {
                            webcamView.setImage(imageToShow);
                        });
                    } catch (Exception e) {
                        System.err.println("Erreur grabber: " + e.getMessage());
                    }
                }
            };

            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            Platform.runLater(() -> statusLabel.setText("Caméra prête."));
        } catch (Exception e) {
            Platform.runLater(() -> statusLabel.setText("Erreur : Impossible d'accéder à la webcam !"));
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCapture(ActionEvent event) {
        statusLabel.setText("Analyse en cours...");
        captureButton.setDisable(true);

        new Thread(() -> {
            try {
                if (isEnrollmentMode) {
                    processEnrollment();
                } else {
                    processLogin();
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Erreur lors de l'analyse.");
                    captureButton.setDisable(false);
                });
            }
        }).start();
    }

    private void processEnrollment() throws Exception {
        capturedFaces.clear();
        int attempts = 0;
        
        while (capturedFaces.size() < 10 && attempts < 30) {
            Frame frame = grabber.grab();
            Mat cvFrame = new org.bytedeco.javacv.OpenCVFrameConverter.ToMat().convert(frame);
            if (cvFrame != null) {
                Mat croppedFace = faceAuthService.extractFace(cvFrame);
                if (croppedFace != null) {
                    capturedFaces.add(croppedFace);
                    final int count = capturedFaces.size();
                    Platform.runLater(() -> statusLabel.setText("Progression : " + count + "/10"));
                }
            }
            attempts++;
            Thread.sleep(100);
        }

        Platform.runLater(() -> {
            if (capturedFaces.size() >= 5) { // 5 est suffisant
                successful = true;
                statusLabel.setText("Visage enregistré avec succès !");
                closeDialog();
            } else {
                statusLabel.setText("Échec: Visage non détecté. Réessayez.");
                captureButton.setDisable(false);
            }
        });
    }

    private void processLogin() throws Exception {
        int attempts = 0;
        recognizedUserId = -1;
        
        while (attempts < 10 && recognizedUserId == -1) {
            Frame frame = grabber.grab();
            Mat cvFrame = new org.bytedeco.javacv.OpenCVFrameConverter.ToMat().convert(frame);
            if (cvFrame != null) {
                Mat croppedFace = faceAuthService.extractFace(cvFrame);
                if (croppedFace != null) {
                    int id = faceAuthService.recognizeFace(croppedFace);
                    if (id > 0) {
                        recognizedUserId = id;
                        successful = true;
                        break;
                    }
                }
            }
            attempts++;
            Thread.sleep(200);
        }

        Platform.runLater(() -> {
            if (successful) {
                statusLabel.setText("Authentification réussie !");
                closeDialog();
            } else {
                statusLabel.setText("Visage non reconnu !");
                captureButton.setDisable(false);
            }
        });
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        successful = false;
        closeDialog();
    }

    private void closeDialog() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) { }
        }

        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {}
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) captureButton.getScene().getWindow();
            stage.close();
        });
    }

    // Getters for caller
    public boolean isSuccessful() { return successful; }
    public List<Mat> getCapturedFaces() { return capturedFaces; }
    public int getRecognizedUserId() { return recognizedUserId; }
}
