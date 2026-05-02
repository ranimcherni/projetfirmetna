package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import models.Commande;
import services.CommandeService;
import utils.AlertUtils;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.sql.Date;

public class AdminCommandesController {

    @FXML private Label totalCommandesLabel;
    @FXML private Label confirmedCommandesLabel;
    @FXML private Label pendingCommandesLabel;
    @FXML private Label cancelledCommandesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label averageOrderValueLabel;
    @FXML private Label maxOrderValueLabel;
    @FXML private Label minOrderValueLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Commande> commandesTable;
    @FXML private TableColumn<Commande, Integer> idCol;
    @FXML private TableColumn<Commande, String> clientCol;
    @FXML private TableColumn<Commande, String> emailCol;
    @FXML private TableColumn<Commande, String> adresseCol;
    @FXML private TableColumn<Commande, BigDecimal> totalCol;
    @FXML private TableColumn<Commande, String> statutCol;
    @FXML private TableColumn<Commande, String> dateCol;
    @FXML private TableColumn<Commande, String> commentaireCol;
    @FXML private TableColumn<Commande, Void> actionsCol;

    private final CommandeService commandeService = new CommandeService();
    private final ObservableList<Commande> commandesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadCommandes();
    }

    private void setupTable() {
        // Configure existing FXML columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        clientCol.setCellValueFactory(new PropertyValueFactory<>("client"));

        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresseLivraison"));

        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setCellFactory(column -> new TableCell<Commande, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%.2f TND", item));
                }
            }
        });

        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutCol.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(item);
                    // Color coding
                    switch (item.toLowerCase()) {
                        case "confirmée": setStyle("-fx-text-fill: green;"); break;
                        case "annulée": setStyle("-fx-text-fill: red;"); break;
                        case "en attente": setStyle("-fx-text-fill: orange;"); break;
                        case "en préparation": setStyle("-fx-text-fill: blue;"); break;
                        default: setStyle(""); break;
                    }
                }
            }
        });

        dateCol.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDate();
            if (date != null) {
                return new SimpleStringProperty(date.toString());
            }
            return new SimpleStringProperty("");
        });

        commentaireCol.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(editBtn, deleteBtn);

            {
                editBtn.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
                pane.setSpacing(5);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        commandesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadCommandes() {
        List<Commande> allCommandes = commandeService.getAll();
        if (allCommandes == null) allCommandes = new ArrayList<>();

        commandesList.clear();
        commandesList.addAll(allCommandes);

        // Force table refresh
        commandesTable.refresh();

        System.out.println("Loaded " + allCommandes.size() + " commandes");
        System.out.println("CommandesList: " + commandesList.size());
        System.out.println("Table items: " + commandesTable.getItems().size());

        // Calculate statistics
        calculateStatistics(allCommandes);
    }

    private void calculateStatistics(List<Commande> allCommandes) {
        if (allCommandes.isEmpty()) {
            totalCommandesLabel.setText("0");
            confirmedCommandesLabel.setText("0");
            pendingCommandesLabel.setText("0");
            cancelledCommandesLabel.setText("0");
            totalRevenueLabel.setText("0 TND");
            averageOrderValueLabel.setText("0 TND");
            maxOrderValueLabel.setText("0 TND");
            minOrderValueLabel.setText("0 TND");
            return;
        }

        // Total commandes
        int total = allCommandes.size();
        totalCommandesLabel.setText(String.valueOf(total));

        // Count by status
        long confirmed = allCommandes.stream()
                .filter(c -> c != null && "confirmée".equalsIgnoreCase(c.getStatut()))
                .count();
        long pending = allCommandes.stream()
                .filter(c -> c != null && "en attente".equalsIgnoreCase(c.getStatut()))
                .count();
        long cancelled = allCommandes.stream()
                .filter(c -> c != null && "annulée".equalsIgnoreCase(c.getStatut()))
                .count();

        confirmedCommandesLabel.setText(String.valueOf(confirmed));
        pendingCommandesLabel.setText(String.valueOf(pending));
        cancelledCommandesLabel.setText(String.valueOf(cancelled));

        // Revenue calculations
        List<BigDecimal> validTotals = allCommandes.stream()
                .filter(c -> c != null && c.getTotal() != null)
                .map(Commande::getTotal)
                .collect(Collectors.toList());

        if (!validTotals.isEmpty()) {
            BigDecimal totalRevenue = validTotals.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalRevenueLabel.setText(String.format("%.2f TND", totalRevenue));

            BigDecimal average = totalRevenue.divide(
                    BigDecimal.valueOf(validTotals.size()),
                    2,
                    BigDecimal.ROUND_HALF_UP
            );
            averageOrderValueLabel.setText(String.format("%.2f TND", average));

            BigDecimal max = validTotals.stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            maxOrderValueLabel.setText(String.format("%.2f TND", max));

            BigDecimal min = validTotals.stream()
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            minOrderValueLabel.setText(String.format("%.2f TND", min));
        }
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous",
                "en préparation",
                "confirmée",
                "en attente",
                "annulée"
        ));
        statusFilter.getSelectionModel().selectFirst();

        FilteredList<Commande> filteredData = new FilteredList<>(commandesList, c -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter(filteredData));
        statusFilter.valueProperty().addListener((obs, oldV, newV) -> updateFilter(filteredData));

        SortedList<Commande> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(commandesTable.comparatorProperty());
        commandesTable.setItems(sortedData);
    }

    private void updateFilter(FilteredList<Commande> filteredData) {
        filteredData.setPredicate(commande -> {
            String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            String status = statusFilter.getValue();

            boolean matchesSearch = search.isEmpty()
                    || (commande.getClient() != null && commande.getClient().toLowerCase().contains(search))
                    || (commande.getEmail() != null && commande.getEmail().toLowerCase().contains(search))
                    || (commande.getAdresseLivraison() != null && commande.getAdresseLivraison().toLowerCase().contains(search));

            boolean matchesStatus = status == null || "Tous".equalsIgnoreCase(status)
                    || (commande.getStatut() != null && commande.getStatut().equalsIgnoreCase(status));

            return matchesSearch && matchesStatus;
        });
    }

    private void handleView(Commande commande) {
        AlertUtils.showSuccess("Commande #" + commande.getId(),
                "Client: " + commande.getClient() + "\n" +
                        "Email: " + commande.getEmail() + "\n" +
                        "Adresse: " + commande.getAdresseLivraison() + "\n" +
                        "Montant: " + String.format("%.2f TND", commande.getTotal()) + "\n" +
                        "Statut: " + commande.getStatut() + "\n" +
                        "Commentaire: " + (commande.getCommentaire() != null ? commande.getCommentaire() : "N/A"));
    }

    private void handleEdit(Commande commande) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Commande");
        dialog.setHeaderText("Modifier les détails de la commande #" + commande.getId());

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("en préparation", "confirmée", "en attente", "annulée");
        statusCombo.setValue(commande.getStatut());

        TextField addressField = new TextField(commande.getAdresseLivraison());
        TextArea commentField = new TextArea(commande.getCommentaire());
        commentField.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Statut:"), 0, 0);
        grid.add(statusCombo, 1, 0);
        grid.add(new Label("Adresse:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Commentaire:"), 0, 2);
        grid.add(commentField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            commande.setStatut(statusCombo.getValue());
            commande.setAdresseLivraison(addressField.getText());
            commande.setCommentaire(commentField.getText());

            try {
                commandeService.update(commande);
                loadCommandes();
                showAlert("Succès", "Commande modifiée avec succès!");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }

    private void handleDelete(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer Commande");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer la commande #" + commande.getId() + "?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commandeService.delete(commande);
                loadCommandes();
                showAlert("Succès", "Commande supprimée avec succès!");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
