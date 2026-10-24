package mliaedu.tp5.dao;

import java.util.List;
import java.util.Optional;

import mliaedu.tp5.domaine.Categorie;

public interface CategorieDao extends IDao<Categorie> {

    Optional<Categorie> parCode(String code);

    /**
     * Charge les rayons avec leurs produits en une seule requête
     * (évite le problème N+1 et les collections non initialisées).
     */
    List<Categorie> avecProduits();

    long nombreDeProduits(int categorieId);
}
