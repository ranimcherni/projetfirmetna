package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Commande;
import models.PanierItem;
import models.Produit;
import models.User;
import services.CommandeService;
import services.ProduitService;
import services.UserService;
import utils.AlertUtils;
import utils.PanierSession;
import utils.UserSession;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.stream.Collectors;

public class CommandeCheckoutController {

    @FXML private Label recapLabel;
    @FXML private Label totalLabel;
    @FXML private TextField clientField;
    @FXML private TextField emailField;
    @FXML private TextField statutField;
    @FXML private TextArea adresseArea;
    @FXML private TextArea commentaireArea;

    private final CommandeService commandeService = new CommandeService();
    private final ProduitService produitService = new ProduitService();
    private Runnable onOrderCreated;

    @FXML
    public void initialize() {
        User user = UserSession.getInstance().getUser();
        if (user != null) {
            clientField.setText(user.getPrenom() + " " + user.getNom());
            emailField.setText(user.getEmail());
        }
        statutField.setText("en préparation");
        recapLabel.setText(PanierSession.getInstance().getItems().stream()
                .map(item -> item.getProduit().getNom() + " x" + item.getQuantite())
                .collect(Collectors.joining(", ")));
        totalLabel.setText(String.format("%.2f DT", PanierSession.getInstance().getTotal().doubleValue()));
    }

    public void setOnOrderCreated(Runnable onOrderCreated) {
        this.onOrderCreated = onOrderCreated;
    }

    @FXML
    private void handleConfirm() {
        try {
            User user = UserSession.getInstance().getUser();
            if (user == null) {
                AlertUtils.showError("Erreur", "Vous devez être connecté pour passer une commande.");
                return;
            }
            if (PanierSession.getInstance().isEmpty()) {
                AlertUtils.showError("Erreur", "Votre panier est vide.");
                return;
            }

            BigDecimal total = PanierSession.getInstance().getTotal();
            String details = PanierSession.getInstance().getItems().stream()
                    .map(PanierItem::getProduit)
                    .map(p -> p.getNom())
                    .collect(Collectors.joining(", "));

            Commande commande = new Commande();
            commande.setDateCommande(new Timestamp(System.currentTimeMillis()));
            commande.setDate(new Date(System.currentTimeMillis()));
            commande.setStatut(statutField.getText().trim().isEmpty() ? "en préparation" : statutField.getText().trim());
            commande.setAdresseLivraison(adresseArea.getText().trim());
            commande.setTotal(total);
            commande.setCommentaire((commentaireArea.getText().trim().isEmpty() ? "" : commentaireArea.getText().trim() + " | ") + "Panier: " + details);
            commande.setClientId(user.getId());
            commande.setEmail(emailField.getText().trim());
            commande.setClient(clientField.getText().trim());

            System.out.println("Enregistrement de la commande...");
            commandeService.add(commande);
            
            // Gamification: Increment user actions for purchase
            new UserService().incrementActionsCount(user.getId());
            
            // Decrement stock for each product in the order
            System.out.println("Mise à jour du stock...");
            for (PanierItem item : PanierSession.getInstance().getItems()) {
                Produit produit = item.getProduit();
                int newStock = produit.getStock() - item.getQuantite();
                if (newStock >= 0) {
                    produit.setStock(newStock);
                    produitService.update(produit);
                }
            }
            
            PanierSession.getInstance().clear();
            AlertUtils.showSuccess("Commande", "Votre commande a été finalisée avec succès.");
            if (onOrderCreated != null) {
                onOrderCreated.run();
            }
            Stage stage = (Stage) totalLabel.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la confirmation de la commande: " + e.getMessage());
            e.printStackTrace();
            AlertUtils.showError("Erreur de Commande", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) totalLabel.getScene().getWindow();
        stage.close();
    }
}
