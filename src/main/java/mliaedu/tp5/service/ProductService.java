package mliaedu.tp5.service;

import java.util.List;
import java.util.Optional;

import mliaedu.tp5.domaine.Product;

/**
 * Cas d'usage du catalogue. Les transactions sont déclarées ici, pas dans le DAO.
 */
public interface ProductService {

    Product ajouter(String reference, String name, double price, String codeCategorie);

    /**
     * Importe un lot de lignes dans un rayon.
     *
     * <p>L'opération est atomique : si une seule ligne est invalide, aucune
     * ligne du lot n'est conservée en base.</p>
     *
     * @return le nombre de produits créés
     */
    int importer(String codeCategorie, List<LigneProduit> lignes);

    Product modifierPrix(String reference, double nouveauPrix);

    Optional<Product> parReference(String reference);

    List<Product> catalogue();

    List<Product> auDessusDe(double seuil);

    double prixMoyen();

    boolean retirer(String reference);

    long nombreDeProduits();
}
