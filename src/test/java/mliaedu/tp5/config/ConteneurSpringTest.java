package mliaedu.tp5.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import mliaedu.tp5.dao.CategorieDao;
import mliaedu.tp5.dao.ProductDao;
import mliaedu.tp5.domaine.Product;
import mliaedu.tp5.service.CategorieService;
import mliaedu.tp5.service.CategorieServiceImpl;
import mliaedu.tp5.service.ProductService;
import mliaedu.tp5.service.ProductServiceImpl;

/**
 * Vérifie que le conteneur Spring démarre et que la chaîne
 * DataSource → SessionFactory → gestionnaire de transactions est complète.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfig.class)
class ConteneurSpringTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PlatformTransactionManager gestionnaire;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private CategorieDao categorieDao;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategorieService categorieService;

    @Test
    @DisplayName("Le conteneur injecte la source de données et la SessionFactory")
    void lesBeansDePersistanceSontInjectes() {
        assertNotNull(dataSource, "la source de données doit être disponible");
        assertNotNull(sessionFactory, "la SessionFactory doit être construite");
    }

    @Test
    @DisplayName("La SessionFactory est celle d'Hibernate 5 et connaît l'entité Product")
    void laSessionFactoryEstCelleDHibernate() {
        String version = org.hibernate.Version.getVersionString();
        assertTrue(version.startsWith("5."), "version Hibernate attendue en 5.x, obtenue " + version);
        assertNotNull(sessionFactory.getMetamodel().entity(Product.class),
                "l'entité Product doit être mappée");
    }

    @Test
    @DisplayName("Les DAO sont enregistrés comme composants Spring")
    void lesDaoSontDesComposants() {
        assertEquals("ProductDaoImpl", AopUtils.getTargetClass(productDao).getSimpleName());
        assertEquals("CategorieDaoImpl", AopUtils.getTargetClass(categorieDao).getSimpleName());
    }

    @Test
    @DisplayName("Les services sont exposés derrière un proxy transactionnel")
    void lesServicesSontProxifiesParSpring() {
        assertTrue(AopUtils.isAopProxy(productService), "ProductService doit être proxifié");
        assertTrue(AopUtils.isAopProxy(categorieService), "CategorieService doit être proxifié");
        assertEquals(ProductServiceImpl.class, AopUtils.getTargetClass(productService));
        assertEquals(CategorieServiceImpl.class, AopUtils.getTargetClass(categorieService));
    }

    @Test
    @DisplayName("Le gestionnaire de transactions est celui d'Hibernate")
    void leGestionnaireEstCeluiDHibernate() {
        assertTrue(gestionnaire instanceof HibernateTransactionManager,
                "attendu HibernateTransactionManager, obtenu " + gestionnaire.getClass().getName());
    }

    @Test
    @DisplayName("La base de données répond")
    void laBaseRepond() throws Exception {
        try (var connexion = dataSource.getConnection()) {
            assertTrue(connexion.isValid(2), "la connexion doit être valide");
            assertFalse(connexion.getMetaData().getDatabaseProductName().isBlank());
        }
    }
}
