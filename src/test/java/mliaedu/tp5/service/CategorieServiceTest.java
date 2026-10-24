package mliaedu.tp5.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import mliaedu.tp5.domaine.Categorie;
import mliaedu.tp5.support.ContexteDeTest;

class CategorieServiceTest extends ContexteDeTest {

    @Test
    @DisplayName("Le code du rayon est normalisé en majuscules")
    void creationNormaliseLeCode() {
        String code = "r" + suffixe();
        Categorie rayon = rayons.creer(code, "Rayon minuscule");

        assertEquals(code.toUpperCase(), rayon.getCode());
        assertTrue(rayons.parCode(code.toLowerCase()).isPresent(), "la recherche doit être insensible à la casse");
    }

    @Test
    @DisplayName("Un code de rayon en double est refusé")
    void codeDupliqueRefuse() {
        String code = "D" + suffixe();
        rayons.creer(code, "Premier rayon");

        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> rayons.creer(code.toLowerCase(), "Doublon"));
        assertTrue(erreur.getMessage().contains("existe déjà"), erreur.getMessage());
    }

    @Test
    @DisplayName("Le libellé du rayon est obligatoire")
    void libelleObligatoire() {
        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> rayons.creer("L" + suffixe(), " "));
        assertTrue(erreur.getMessage().contains("obligatoire"), erreur.getMessage());
    }

    @Test
    @DisplayName("L'inventaire charge les produits avec leur rayon en une requête")
    void inventaireChargeLesProduits() {
        String code = "J" + suffixe();
        rayons.creer(code, "Rayon jointure");
        produits.ajouter("J-" + suffixe(), "Produit joint", 15.00, code);

        List<Categorie> inventaire = rayons.inventaire();
        Categorie rayon = inventaire.stream()
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError("le rayon " + code + " doit figurer dans l'inventaire"));

        assertEquals(1, rayon.getNombreProduits(),
                "la collection doit être initialisée par le join fetch, sans LazyInitializationException");
    }

    @Test
    @DisplayName("Un rayon contenant des produits ne peut pas être supprimé")
    void suppressionRefuseeSiNonVide() {
        String code = "K" + suffixe();
        rayons.creer(code, "Rayon occupé");
        produits.ajouter("K-" + suffixe(), "Produit bloquant", 12.00, code);

        IllegalStateException erreur = assertThrows(IllegalStateException.class,
                () -> rayons.supprimer(code));
        assertTrue(erreur.getMessage().contains("contient encore"), erreur.getMessage());
        assertTrue(rayons.parCode(code).isPresent(), "le rayon doit toujours exister");
    }

    @Test
    @DisplayName("Un rayon vide est supprimé")
    void suppressionDUnRayonVide() {
        String code = "V" + suffixe();
        rayons.creer(code, "Rayon vide");

        assertTrue(rayons.supprimer(code));
        assertTrue(rayons.parCode(code).isEmpty());
    }

    @Test
    @DisplayName("Un rayon inconnu ne peut pas être supprimé")
    void suppressionRayonInconnu() {
        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> rayons.supprimer("INEXISTANT-" + suffixe()));
        assertTrue(erreur.getMessage().contains("inconnu"), erreur.getMessage());
    }
}
