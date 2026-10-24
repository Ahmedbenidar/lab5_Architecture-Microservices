package mliaedu.tp5.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mliaedu.tp5.domaine.Product;
import mliaedu.tp5.support.ContexteDeTest;

class ProductServiceTest extends ContexteDeTest {

    private String codeRayon;

    @BeforeEach
    void preparerRayon() {
        codeRayon = "T" + suffixe();
        rayons.creer(codeRayon, "Rayon de test " + codeRayon);
    }

    @Test
    @DisplayName("L'ajout d'un produit renseigne son identifiant et son rayon")
    void ajoutPersisteLeProduit() {
        String reference = "REF-" + suffixe();
        Product produit = produits.ajouter(reference, "Produit de test", 19.90, codeRayon);

        assertNotNull(produit.getId(), "l'identifiant doit être généré par la base");
        assertEquals(reference, produit.getReference());
        assertEquals(codeRayon, produit.getCodeCategorie());

        Optional<Product> relu = produits.parReference(reference);
        assertTrue(relu.isPresent(), "le produit doit être relu par sa référence");
        assertEquals(19.90, relu.get().getPriceAsDouble(), 0.001);
    }

    @Test
    @DisplayName("Un prix négatif est refusé avant toute écriture")
    void prixNegatifRefuse() {
        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> produits.ajouter("REF-" + suffixe(), "Prix faux", -3, codeRayon));
        assertTrue(erreur.getMessage().contains("négatif"), erreur.getMessage());
    }

    @Test
    @DisplayName("Une référence déjà utilisée est refusée")
    void referenceDupliqueeRefusee() {
        String reference = "REF-" + suffixe();
        produits.ajouter(reference, "Premier", 10, codeRayon);

        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> produits.ajouter(reference, "Second", 20, codeRayon));
        assertTrue(erreur.getMessage().contains("déjà utilisée"), erreur.getMessage());
    }

    @Test
    @DisplayName("Un rayon inexistant est refusé")
    void rayonInconnuRefuse() {
        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> produits.ajouter("REF-" + suffixe(), "Orphelin", 10, "INCONNU-" + suffixe()));
        assertTrue(erreur.getMessage().contains("n'existe pas"), erreur.getMessage());
    }

    @Test
    @DisplayName("Le nom du produit est obligatoire")
    void nomObligatoire() {
        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> produits.ajouter("REF-" + suffixe(), "  ", 10, codeRayon));
        assertTrue(erreur.getMessage().contains("obligatoire"), erreur.getMessage());
    }

    @Test
    @DisplayName("La modification du prix est persistée")
    void modificationDuPrix() {
        String reference = "REF-" + suffixe();
        produits.ajouter(reference, "Produit à réévaluer", 50.00, codeRayon);

        Product misAJour = produits.modifierPrix(reference, 42.50);

        assertEquals(42.50, misAJour.getPriceAsDouble(), 0.001);
        assertEquals(42.50, produits.parReference(reference).orElseThrow().getPriceAsDouble(), 0.001);
    }

    @Test
    @DisplayName("Le filtre par prix et le prix moyen sont cohérents")
    void indicateursDePrix() {
        String marqueur = suffixe();
        produits.ajouter("BAS-" + marqueur, "Petit prix", 10.00, codeRayon);
        produits.ajouter("HAUT-" + marqueur, "Gros prix", 300.00, codeRayon);

        List<Product> chers = produits.auDessusDe(200);
        assertTrue(chers.stream().anyMatch(p -> p.getReference().equals("HAUT-" + marqueur)));
        assertFalse(chers.stream().anyMatch(p -> p.getReference().equals("BAS-" + marqueur)));
        assertTrue(produits.prixMoyen() > 0, "le catalogue doit avoir un prix moyen positif");
    }

    @Test
    @DisplayName("Le retrait d'un produit le rend introuvable")
    void retraitDuProduit() {
        String reference = "REF-" + suffixe();
        produits.ajouter(reference, "Produit éphémère", 5.00, codeRayon);

        assertTrue(produits.retirer(reference));
        assertTrue(produits.parReference(reference).isEmpty());
        assertFalse(produits.retirer(reference), "un second retrait ne doit rien supprimer");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("L'import d'un lot est atomique : une ligne invalide annule tout le lot")
    void importAtomiqueDuLot() {
        String code = "I" + suffixe();
        rayons.creer(code, "Rayon import " + code);
        long avant = produits.nombreDeProduits();
        try {
            IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                    () -> produits.importer(code, List.of(
                            new LigneProduit("LOT-1-" + code, "Ligne valide", 25.00),
                            new LigneProduit("LOT-2-" + code, "Ligne invalide", -5.00))));
            assertTrue(erreur.getMessage().contains("négatif"), erreur.getMessage());
            assertEquals(avant, produits.nombreDeProduits(),
                    "la première ligne du lot doit être annulée avec la seconde");
        } finally {
            rayons.supprimer(code);
        }
    }
}
