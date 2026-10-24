package mliaedu.tp5.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import mliaedu.tp5.domaine.Categorie;

@Repository
public class CategorieDaoImpl extends AbstractDao<Categorie> implements CategorieDao {

    public CategorieDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory, Categorie.class);
    }

    @Override
    public Optional<Categorie> parCode(String code) {
        return session()
                .createQuery("from Categorie c where lower(c.code) = lower(:code)", Categorie.class)
                .setParameter("code", code)
                .uniqueResultOptional();
    }

    @Override
    public List<Categorie> avecProduits() {
        return session()
                .createQuery("select distinct c from Categorie c left join fetch c.produits order by c.code",
                        Categorie.class)
                .getResultList();
    }

    @Override
    public long nombreDeProduits(int categorieId) {
        Long total = session()
                .createQuery("select count(p) from Product p where p.categorie.id = :id", Long.class)
                .setParameter("id", (long) categorieId)
                .getSingleResult();
        return total == null ? 0L : total;
    }
}
