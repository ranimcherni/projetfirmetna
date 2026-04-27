package test;

import models.User;
import services.UserService;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserService us = new UserService();

        try {

            System.out.println("\n--- [1] TEST AJOUT ---");
            User testUser = new User("test_crud@esprit.tn", "pass123", "ROLE_USER", "Zardi", "Ranim", "Ariana",
                    "Etudiante Esprit", "Informatique");
            testUser.setTelephone("22113344");
            us.add(testUser);

            System.out.println("\n--- [2] TEST AFFICHAGE ---");
            List<User> users = us.getAll();
            for (User u : users) {
                System.out.println(u);
            }

            if (!users.isEmpty()) {
                System.out.println("\n--- [3] TEST MODIFICATION ---");
                User lastUser = users.get(users.size() - 1);
                lastUser.setNom(lastUser.getNom() + " (Modifié)");
                us.update(lastUser);
                System.out.println("Nouveau nom : " + lastUser.getNom());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test CRUD : " + e.getMessage());
        }
    }
}
