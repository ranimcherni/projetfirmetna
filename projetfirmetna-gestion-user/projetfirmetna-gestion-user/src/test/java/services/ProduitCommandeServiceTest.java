package services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProduitCommandeServiceTest {

    @Test
    void produitServiceShouldConnectAndReadTable() {
        ProduitService produitService = new ProduitService();
        assertNotNull(produitService);
        assertDoesNotThrow(produitService::getAll);
    }

    @Test
    void commandeServiceShouldConnectAndReadTable() {
        CommandeService commandeService = new CommandeService();
        assertNotNull(commandeService);
        assertDoesNotThrow(commandeService::getAll);
    }
}
