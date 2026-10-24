package mliaedu.tp5.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Contrat d'accès aux données commun à toutes les entités du microservice.
 *
 * <p>Le paramètre de type {@code T} évite de dupliquer les opérations CRUD :
 * chaque entité concrète n'ajoute que ses requêtes métier.</p>
 *
 * @param <T> type de l'entité gérée
 */
public interface IDao<T> {

    /**
     * Rend l'entité persistante ; l'identifiant est renseigné par la base.
     *
     * @return la même instance, identifiant compris
     */
    T enregistrer(T entite);

    /**
     * Fusionne l'état d'une entité détachée dans la session courante.
     *
     * @return l'instance gérée
     */
    T mettreAJour(T entite);

    /**
     * @return {@code false} si aucune ligne ne correspond à l'identifiant
     */
    boolean supprimer(Serializable id);

    Optional<T> parId(Serializable id);

    List<T> tous();

    long compter();
}
