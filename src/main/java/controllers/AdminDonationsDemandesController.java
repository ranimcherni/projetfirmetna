package controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import models.Demande;
import models.Offre;
import models.User;
import services.DemandeService;
import services.OffreService;
import services.UserService;
<<<<<<< HEAD
=======
import utils.NavigationService;
import utils.UserSession;
import javafx.event.ActionEvent;
>>>>>>> gestion-user

public class AdminDonationsDemandesController {

    @FXML
<<<<<<< HEAD
=======
    private void handlePartenariats(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/FrontPartnerView.fxml", "Espace Partenaires");
    }

    @FXML
    private void handleAccueil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void handleProduits(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Produits");
    }

    @FXML
    private void handleProduitsVegetaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("vegetale");
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Vegetaux");
    }

    @FXML
    private void handleProduitsAnimaux(ActionEvent event) {
        utils.ProductNavigationState.setSelectedType("animale");
        NavigationService.switchScene(event, "/esprit/tn/fxml/product_marketplace.fxml", "Marketplace - Animaux");
    }

    @FXML
    private void handleForum(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum");
    }

    @FXML
    private void handleEvenements(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }

    @FXML
    private void handleDons(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_donations_offres.fxml", "Donations & Solidarité");
    }

    @FXML
    private void handleSwitchToOffres(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_donations_offres.fxml", "Donations & Solidarité");
    }

    @FXML
    private void handleProfil(ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
>>>>>>> gestion-user
    private TableView<Demande> demandeTable;
    @FXML
    private TableColumn<Demande, Integer> colId;
    @FXML
    private TableColumn<Demande, Integer> colOffreId;
    @FXML
    private TableColumn<Demande, Integer> colUserId;
    @FXML
    private TableColumn<Demande, Integer> colQuantite;
    @FXML
    private TableColumn<Demande, String> colStatut;
    @FXML
    private TableColumn<Demande, String> colCreatedAt;
    @FXML
    private TableColumn<Demande, String> colProduit;

    @FXML
    private TextField offreIdField;
    @FXML
    private TextField userIdField;
    @FXML
    private TextField quantiteField;
    @FXML
    private TextField statutField;
    @FXML
    private javafx.scene.control.Label errorOffreId;
    @FXML
    private javafx.scene.control.Label errorUserId;
    @FXML
    private javafx.scene.control.Label errorQuantite;
    @FXML
    private javafx.scene.control.Label errorStatut;
    @FXML
    private TextField searchField;
    @FXML
    private CheckBox stockOnlyCheck;
    @FXML
    private ComboBox<String> sortCombo;

    private final DonationController donationController = new DonationController();
    private final DemandeService demandeService = new DemandeService();
    private final OffreService offreService = new OffreService();
    private final UserService userService = new UserService();
    private final ObservableList<Demande> masterData = FXCollections.observableArrayList();
    private FilteredList<Demande> filteredData;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        colOffreId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getOffre().getId()).asObject());
        colUserId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getDemandeur().getId()).asObject());
        colQuantite.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getQuantiteDemandee()).asObject());
        colStatut.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatut()));
        colCreatedAt.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getCreatedAt())));
        colProduit.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getOffre() != null ? safe(cell.getValue().getOffre().getCategorie()) : ""
        ));

        setupFilterAndSort();
        refreshTable();

        demandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                offreIdField.setText(String.valueOf(selected.getOffre().getId()));
                userIdField.setText(String.valueOf(selected.getDemandeur().getId()));
                quantiteField.setText(String.valueOf(selected.getQuantiteDemandee()));
                statutField.setText(selected.getStatut());
            }
        });
    }

    private void setupFilterAndSort() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "Plus recent",
                "Quantite croissante",
                "Quantite decroissante",
                "Produit A-Z"
        ));
        sortCombo.getSelectionModel().selectFirst();

        filteredData = new FilteredList<>(masterData, item -> true);
        SortedList<Demande> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(demandeTable.comparatorProperty());
        demandeTable.setItems(sortedData);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        stockOnlyCheck.selectedProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        boolean stockOnly = stockOnlyCheck.isSelected();

        filteredData.setPredicate(demande -> {
            String produit = demande.getOffre() != null ? safe(demande.getOffre().getCategorie()) : "";
            String description = demande.getOffre() != null ? safe(demande.getOffre().getDescription()) : "";
            String statut = safe(demande.getStatut());

            boolean matchesText = keyword.isEmpty()
                    || produit.toLowerCase().contains(keyword)
                    || description.toLowerCase().contains(keyword)
                    || statut.toLowerCase().contains(keyword);

            boolean hasStock = demande.getOffre() != null
                    && demande.getOffre().getQuantite() > 0
                    && demande.isDisponible();
            boolean matchesStock = !stockOnly || hasStock;
            return matchesText && matchesStock;
        });

        String sort = sortCombo.getValue();
        if ("Quantite croissante".equals(sort)) {
            filteredData.getSource().sort((a, b) -> Integer.compare(a.getQuantiteDemandee(), b.getQuantiteDemandee()));
        } else if ("Quantite decroissante".equals(sort)) {
            filteredData.getSource().sort((a, b) -> Integer.compare(b.getQuantiteDemandee(), a.getQuantiteDemandee()));
        } else if ("Produit A-Z".equals(sort)) {
            filteredData.getSource().sort((a, b) -> {
                String pa = a.getOffre() != null ? safe(a.getOffre().getCategorie()) : "";
                String pb = b.getOffre() != null ? safe(b.getOffre().getCategorie()) : "";
                return pa.compareToIgnoreCase(pb);
            });
        } else {
            filteredData.getSource().sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInput()) return;
        Integer offreId = Integer.parseInt(offreIdField.getText().trim());
        Integer userId = Integer.parseInt(userIdField.getText().trim());
        Integer quantite = Integer.parseInt(quantiteField.getText().trim());

        boolean created = donationController.createDemande(offreId, userId, quantite, statutField.getText().trim(), true);
        if (!created) {
            showError("Creation echouee. Verifiez les IDs et la quantite demandee (<= offre).");
            return;
        }
        refreshTable();
        clearForm();
    }

    @FXML
    private void handleUpdate() {
        Demande selected = demandeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une demande a modifier.");
            return;
        }

        if (!validateInput()) return;
        Integer offreId = Integer.parseInt(offreIdField.getText().trim());
        Integer userId = Integer.parseInt(userIdField.getText().trim());
        Integer quantite = Integer.parseInt(quantiteField.getText().trim());

        Offre offre = offreService.getById(offreId);
<<<<<<< HEAD
        User user = userService.getById(userId);
=======
        User user = userService.getUserById(userId);
>>>>>>> gestion-user
        if (offre == null || user == null) {
            showError("Mise a jour echouee. Offre/User introuvable.");
            return;
        }

        if (!demandeService.isQuantiteDemandeeValide(offreId, quantite)) {
            showError("Quantite demandee invalide (doit etre <= quantite de l'offre).");
            return;
        }

        selected.setOffre(offre);
        selected.setDemandeur(user);
        selected.setQuantiteDemandee(quantite);
        selected.setStatut(statutField.getText().trim().isEmpty() ? "EN_ATTENTE" : statutField.getText().trim());
        selected.setDisponible(true);
        demandeService.update(selected);
        refreshTable();
        clearForm();
    }

    @FXML
    private void handleDelete() {
        Demande selected = demandeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner une demande a supprimer.");
            return;
        }
        donationController.deleteDemande(selected);
        refreshTable();
        clearForm();
    }

    @FXML
    private void handleClear() {
        clearForm();
        demandeTable.getSelectionModel().clearSelection();
    }

<<<<<<< HEAD
=======
    @FXML
    private void handleOpenChatbot(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/esprit/tn/fxml/chatbot.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Assistant Intelligent");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.NONE);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Impossible de charger le Chatbot : " + e.getMessage());
        }
    }

>>>>>>> gestion-user
    private void refreshTable() {
        masterData.setAll(demandeService.getAll());
        applyFilters();
    }

    private void clearForm() {
        offreIdField.clear();
        userIdField.clear();
        quantiteField.clear();
        statutField.clear();
        
        if (errorOffreId != null) {
            errorOffreId.setVisible(false);
            errorOffreId.setManaged(false);
            errorUserId.setVisible(false);
            errorUserId.setManaged(false);
            errorQuantite.setVisible(false);
            errorQuantite.setManaged(false);
            errorStatut.setVisible(false);
            errorStatut.setManaged(false);
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        try {
            int oid = Integer.parseInt(offreIdField.getText().trim());
            if (oid <= 0) {
                errorOffreId.setText("ID doit être > 0");
                errorOffreId.setVisible(true);
                errorOffreId.setManaged(true);
                isValid = false;
            } else {
                errorOffreId.setVisible(false);
                errorOffreId.setManaged(false);
            }
        } catch (NumberFormatException e) {
            errorOffreId.setText("ID invalide");
            errorOffreId.setVisible(true);
            errorOffreId.setManaged(true);
            isValid = false;
        }

        try {
            int uid = Integer.parseInt(userIdField.getText().trim());
            if (uid <= 0) {
                errorUserId.setText("ID doit être > 0");
                errorUserId.setVisible(true);
                errorUserId.setManaged(true);
                isValid = false;
            } else {
                errorUserId.setVisible(false);
                errorUserId.setManaged(false);
            }
        } catch (NumberFormatException e) {
            errorUserId.setText("ID invalide");
            errorUserId.setVisible(true);
            errorUserId.setManaged(true);
            isValid = false;
        }

        try {
            int qte = Integer.parseInt(quantiteField.getText().trim());
            if (qte <= 0) {
                errorQuantite.setText("Quantité > 0 requise");
                errorQuantite.setVisible(true);
                errorQuantite.setManaged(true);
                isValid = false;
            } else {
                errorQuantite.setVisible(false);
                errorQuantite.setManaged(false);
            }
        } catch (NumberFormatException e) {
            errorQuantite.setText("Quantité invalide");
            errorQuantite.setVisible(true);
            errorQuantite.setManaged(true);
            isValid = false;
        }

        if (statutField.getText().trim().isEmpty()) {
            errorStatut.setText("Statut requis");
            errorStatut.setVisible(true);
            errorStatut.setManaged(true);
            isValid = false;
        } else {
            errorStatut.setVisible(false);
            errorStatut.setManaged(false);
        }

        return isValid;
    }

    private Integer parseIntField(TextField field, String errorMessage) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value <= 0) {
                showError(errorMessage);
                return null;
            }
            return value;
        } catch (Exception e) {
            showError(errorMessage);
            return null;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Operation impossible");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
