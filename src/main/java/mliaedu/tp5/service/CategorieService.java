package mliaedu.tp5.service;

import java.util.List;
import java.util.Optional;

import mliaedu.tp5.domaine.Categorie;

public interface CategorieService {

    Categorie creer(String code, String libelle);

    Optional<Categorie> parCode(String code);

    /**
     * Inventaire complet : rayons et produits chargés en une requête.
     */
    List<Categorie> inventaire();

    /**
     * @throws IllegalStateException si le rayon contient encore des produits
     */
    boolean supprimer(String code);

    long nombreDeRayons();
}
