package mliaedu.tp5.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import mliaedu.tp5.domaine.Product;

@Repository
public class ProductDaoImpl extends AbstractDao<Product> implements ProductDao {

    public ProductDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory, Product.class);
    }

    @Override
    public Optional<Product> parReference(String reference) {
        return session()
                .createQuery("from Product p where p.reference = :reference", Product.class)
                .setParameter("reference", reference)
                .uniqueResultOptional();
    }

    @Override
    public boolean referenceExiste(String reference) {
        Long total = session()
                .createQuery("select count(p) from Product p where lower(p.reference) = lower(:reference)", Long.class)
                .setParameter("reference", reference)
                .getSingleResult();
        return total != null && total > 0;
    }

    @Override
    public List<Product> duRayon(String codeCategorie) {
        return session()
                .createQuery("from Product p where p.categorie.code = :code order by p.name", Product.class)
                .setParameter("code", codeCategorie)
                .getResultList();
    }

    @Override
    public List<Product> plusChersQue(BigDecimal seuil) {
        return session()
                .createQuery("from Product p where p.price > :seuil order by p.price desc", Product.class)
                .setParameter("seuil", seuil)
                .getResultList();
    }

    @Override
    public BigDecimal prixMoyen() {
        Double moyenne = session()
                .createQuery("select avg(p.price) from Product p", Double.class)
                .getSingleResult();
        return moyenne == null ? BigDecimal.ZERO : BigDecimal.valueOf(moyenne);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> nombreParRayon() {
        return session()
                .createQuery("select c.code, count(p) from Product p join p.categorie c"
                        + " group by c.code order by c.code")
                .getResultList();
    }
}
