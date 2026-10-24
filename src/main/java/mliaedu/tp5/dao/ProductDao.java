package mliaedu.tp5.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import mliaedu.tp5.domaine.Product;

/**
 * Requêtes propres au catalogue : recherche par référence, par rayon,
 * filtrage par prix et indicateurs d'agrégation.
 */
public interface ProductDao extends IDao<Product> {

    Optional<Product> parReference(String reference);

    boolean referenceExiste(String reference);

    List<Product> duRayon(String codeCategorie);

    List<Product> plusChersQue(BigDecimal seuil);

    BigDecimal prixMoyen();

    List<Object[]> nombreParRayon();
}
