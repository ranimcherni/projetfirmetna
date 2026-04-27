package services;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatbotService {

    private final Map<String, String> answers = new LinkedHashMap<>();

    public ChatbotService() {
        answers.put("projet", "Firmetna est une plateforme de marché fermier. " +
                "Les agriculteurs peuvent partager leurs produits, " +
                "les clients peuvent acheter, et les donateurs peuvent soutenir.");
        answers.put("idee", "L'idée du projet est de créer un espace sécurisé " +
                "pour vendre des produits fermiers locaux et faciliter la gestion des commandes.");
        answers.put("produit", "Tu peux parcourir les produits, ajouter au panier, " +
                "et passer commande selon la quantité disponible.");
        answers.put("panier", "Le panier stocke tes produits sélectionnés. " +
                "Tu peux ensuite confirmer la commande et payer.");
        answers.put("commande", "Une commande se crée après confirmation du panier. " +
                "Le stock est mis à jour automatiquement.");
        answers.put("role", "Il y a plusieurs rôles : Admin, Agriculteur, Client et Donateur. " +
                "Les utilisateurs non-admin voient le frontend et le chatbot.");
        answers.put("contact", "Pour plus d'informations, utilise le formulaire du site ou contacte l'administrateur.");
    }

    public String ask(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Pose-moi une question sur le projet ou l'utilisation de la plateforme.";
        }
        String lower = question.toLowerCase();

        for (Map.Entry<String, String> entry : answers.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Je suis encore en apprentissage. Pose-moi une autre question sur le projet, les produits, le panier ou les commandes.";
    }
}