package services;

import models.User;
import org.junit.jupiter.api.*;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    static UserService service;
    static int idUserTest = -1; // Pour garder la trace de l'utilisateur à modifier/supprimer

    @BeforeAll
    static void setup() {
        service = new UserService();
    }

    @Test
    @Order(1)
    void testAjouterUser() {
        User u = new User();
        u.setNom("TestNom");
        u.setPrenom("TestPrenom");
        u.setEmail("testunitaire@firmetna.com");
        u.setPassword("Pass123!");
        u.setRole("Client");
        u.setStatus("Actif");

        // 1. Appel de la méthode
        service.add(u);

        // 2. Récupération et vérification
        List<User> users = service.getAll();
        assertFalse(users.isEmpty());

        boolean trouve = false;
        for (User user : users) {
            if ("testunitaire@firmetna.com".equals(user.getEmail())) {
                trouve = true;
                idUserTest = user.getId(); // On sauvegarde l'ID généré pour les tests suivants
                break;
            }
        }
        assertTrue(trouve, "L'utilisateur n'a pas été trouvé après son ajout.");
    }

    @Test
    @Order(2)
    void testModifierUser() {
        // Sécurité : vérifier qu'on a bien un utilisateur à modifier
        assertTrue(idUserTest != -1, "ID introuvable. Le test d'ajout a dû échouer.");

        User u = new User();
        u.setId(idUserTest);
        u.setNom("NomModifie");
        u.setPrenom("TestPrenom");
        u.setEmail("testunitaire@firmetna.com");
        u.setPassword("Pass123!");
        u.setRole("Donateur");
        
        // 1. Test de la modification
        service.update(u);

        // 2. Vérification
        List<User> users = service.getAll();
        boolean trouve = users.stream()
                .anyMatch(user -> user.getNom().equals("NomModifie") && user.getId() == idUserTest);

        assertTrue(trouve, "Le nom n'a pas été modifié en 'NomModifie'.");
    }

    @Test
    @Order(3)
    void testSupprimerUser() {
        assertTrue(idUserTest != -1, "ID introuvable.");

        User u = new User();
        u.setId(idUserTest);

        // 1. Appel suppression
        service.delete(u);

        // 2. Vérification (il ne doit plus exister)
        List<User> users = service.getAll();
        boolean existe = users.stream()
                .anyMatch(user -> user.getId() == idUserTest);

        assertFalse(existe, "L'utilisateur existe encore, la suppression a échoué.");
    }
}
