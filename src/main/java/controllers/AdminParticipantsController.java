package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Evenement;
import models.User;
import services.ParticipationService;

public class AdminParticipantsController {

    @FXML private Label titleLabel;
    @FXML private TableView<User> participantsTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> nomCol;
    @FXML private TableColumn<User, String> prenomCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> phoneCol;

    private final ParticipationService participationService = new ParticipationService();
    private final ObservableList<User> participantsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
    }

    public void setEvenement(Evenement ev) {
        titleLabel.setText("Participants : " + ev.getNom());
        loadParticipants(ev.getId());
    }

    private void loadParticipants(int evenementId) {
        participantsList.setAll(participationService.getParticipantsByEvenement(evenementId));
        participantsTable.setItems(participantsList);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) participantsTable.getScene().getWindow();
        stage.close();
    }
}
