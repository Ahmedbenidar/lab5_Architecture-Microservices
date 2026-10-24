package mliaedu.tp5.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Squelette des DAO Hibernate : une seule implémentation du CRUD pour toutes
 * les entités dérivées.
 *
 * <p>La session est obtenue par {@code getCurrentSession()} : c'est Spring qui
 * l'ouvre, la ferme et la relie à la transaction en cours
 * ({@code @Transactional}). Aucun {@code commit} n'apparaît donc ici.</p>
 *
 * @param <T> entité gérée
 */
public abstract class AbstractDao<T> implements IDao<T> {

    private final SessionFactory sessionFactory;
    private final Class<T> typeEntite;

    protected AbstractDao(SessionFactory sessionFactory, Class<T> typeEntite) {
        this.sessionFactory = sessionFactory;
        this.typeEntite = typeEntite;
    }

    protected final Session session() {
        return sessionFactory.getCurrentSession();
    }

    protected final Class<T> typeEntite() {
        return typeEntite;
    }

    protected final String nomEntite() {
        return typeEntite.getSimpleName();
    }

    @Override
    public T enregistrer(T entite) {
        session().persist(entite);
        return entite;
    }

    @Override
    public T mettreAJour(T entite) {
        @SuppressWarnings("unchecked")
        T fusionnee = (T) session().merge(entite);
        return fusionnee;
    }

    @Override
    public boolean supprimer(Serializable id) {
        T entite = session().get(typeEntite, id);
        if (entite == null) {
            return false;
        }
        session().remove(entite);
        return true;
    }

    @Override
    public Optional<T> parId(Serializable id) {
        return Optional.ofNullable(session().get(typeEntite, id));
    }

    @Override
    public List<T> tous() {
        return session()
                .createQuery("from " + nomEntite() + " order by id", typeEntite)
                .getResultList();
    }

    @Override
    public long compter() {
        Long total = session()
                .createQuery("select count(e) from " + nomEntite() + " e", Long.class)
                .getSingleResult();
        return total == null ? 0L : total;
    }
}
