package mliaedu.tp5;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import mliaedu.tp5.config.ApplicationConfig;
import mliaedu.tp5.dao.CategorieDao;
import mliaedu.tp5.dao.ProductDao;
import mliaedu.tp5.domaine.Categorie;
import mliaedu.tp5.domaine.Product;
import mliaedu.tp5.service.CategorieService;
import mliaedu.tp5.service.LigneProduit;
import mliaedu.tp5.service.ProductService;

/**
 * Scénario de vérification du microservice catalogue.
 *
 * <p>Le programme démarre le conteneur Spring, puis contrôle chaque exigence du
 * TP : injection des dépendances, généricité du DAO, transactions déclaratives
 * et validations métier. Chaque contrôle affiche la valeur attendue et la
 * valeur réellement observée ; le code de sortie vaut 0 si tout est conforme,
 * 1 sinon.</p>
 */
public class CatalogueDemo {

    private static final String LIGNE = "=".repeat(90);
    private static final String SOUS_LIGNE = "-".repeat(80);

    private int controles;
    private int echecs;

    public static void main(String[] args) {
        calibrageJournalisation();
        CatalogueDemo demo = new CatalogueDemo();
        int code = demo.executer();
        System.exit(code);
    }

    private int executer() {
        try (AnnotationConfigApplicationContext contexte =
                     new AnnotationConfigApplicationContext(ApplicationConfig.class)) {

            ProductService produits = contexte.getBean(ProductService.class);
            CategorieService rayons = contexte.getBean(CategorieService.class);
            SessionFactory sessionFactory = contexte.getBean(SessionFactory.class);
            PlatformTransactionManager gestionnaire = contexte.getBean(PlatformTransactionManager.class);
            DataSource source = contexte.getBean(DataSource.class);
            ProductDao produitDao = contexte.getBean(ProductDao.class);
            CategorieDao rayonDao = contexte.getBean(CategorieDao.class);
            TransactionTemplate transaction = new TransactionTemplate(gestionnaire);

            entete(contexte, source, sessionFactory);
            etape(0, "Conteneur Spring : injection et câblage des dépendances");
            verifierConteneur(contexte, produits, rayons, sessionFactory, gestionnaire, source);

            etape(1, "Remise à zéro du catalogue");
            purger(transaction, sessionFactory);
            comparer("Produits restants après purge", 0L, dansTransaction(transaction, produitDao::compter));
            comparer("Rayons restants après purge", 0L, dansTransaction(transaction, rayonDao::compter));

            etape(2, "Création des rayons puis des produits (couche service)");
            Categorie info = rayons.creer("INFO", "Informatique réseau");
            Categorie reseau = rayons.creer("RESEAU", "Réseau et connectique");
            System.out.println("  rayons créés      -> " + info + " | " + reseau);
            Product carte = produits.ajouter("SKU-100", "Carte réseau 1 Gb", 39.90, "INFO");
            produits.ajouter("SKU-101", "Switch 8 ports", 89.00, "INFO");
            produits.ajouter("SKU-102", "Routeur Wi-Fi 6", 129.99, "RESEAU");
            produits.ajouter("SKU-103", "Clé USB-C 64 Go", 12.50, "reseau");
            System.out.println("  produits créés    -> " + produits.catalogue().size() + " lignes");
            comparer("Rayons persistés", 2L, rayons.nombreDeRayons());
            comparer("Produits persistés", 4L, produits.nombreDeProduits());
            comparer("Identifiant généré par la base", 1L, carte.getId());
            comparer("Rayon rattaché au produit", "INFO",
                    produits.parReference("SKU-100").map(Product::getCodeCategorie).orElse("-"));

            etape(3, "Lecture, mise à jour et indicateurs");
            Optional<Product> routeur = produits.parReference("SKU-102");
            System.out.println("  parReference      -> " + routeur.map(Product::toString).orElse("absent"));
            comparer("Produit retrouvé par sa référence", "Routeur Wi-Fi 6",
                    routeur.map(Product::getName).orElse("-"));
            Product misAJour = produits.modifierPrix("SKU-102", 119.99);
            comparer("Nouveau prix enregistré", 119.99, misAJour.getPriceAsDouble());
            comparer("Taille du catalogue", 4, produits.catalogue().size());
            comparer("Produits au-dessus de 80 EUR", 2, produits.auDessusDe(80).size());
            comparer("Prix moyen du catalogue", 65.35, produits.prixMoyen());

            etape(4, "Requêtes HQL du DAO générique et jointure");
            comparer("Produits du rayon INFO", 2,
                    dansTransaction(transaction, () -> produitDao.duRayon("INFO").size()));
            comparer("Répartition par rayon", "INFO=2, RESEAU=2",
                    repartition(dansTransaction(transaction, produitDao::nombreParRayon)));
            List<Categorie> inventaire = rayons.inventaire();
            comparer("Rayons chargés avec leurs produits", 4, inventaire.stream()
                    .mapToInt(Categorie::getNombreProduits).sum());
            Categorie rayonInfo = info;
            comparer("Produits du rayon INFO (via le rayon)", 2,
                    dansTransaction(transaction, () -> rayonDao.nombreDeProduits(rayonInfo.getId().intValue())));
            comparer("Comptage générique du DAO", 2L, dansTransaction(transaction, rayonDao::compter));

            etape(5, "Validations métier et transactions déclaratives");
            verifierRejet("Prix négatif refusé", () -> produits.ajouter("SKU-200", "Test", -1, "INFO"),
                    "négatif");
            verifierRejet("Référence déjà utilisée refusée",
                    () -> produits.ajouter("SKU-100", "Doublon", 10, "INFO"), "déjà utilisée");
            verifierRejet("Rayon inconnu refusé",
                    () -> produits.ajouter("SKU-201", "Test", 10, "INCONNU"), "n'existe pas");
            long avant = produits.nombreDeProduits();
            verifierRejet("Import atomique : une ligne invalide annule le lot",
                    () -> produits.importer("INFO", List.of(
                            new LigneProduit("SKU-300", "Ligne valide", 25.00),
                            new LigneProduit("SKU-301", "Ligne invalide", -5.00))),
                    "négatif");
            comparer("Nombre de produits après rollback du lot", avant, produits.nombreDeProduits());
            verifierContrainteSql(transaction, produitDao, avant);

            etape(6, "Suppressions et intégrité référentielle");
            comparer("Produit retiré", true, produits.retirer("SKU-103"));
            comparer("Catalogue après suppression", 3L, produits.nombreDeProduits());
            verifierRejet("Rayon non vide protégé", () -> rayons.supprimer("INFO"), "contient encore");
            Categorie promo = rayons.creer("PROMO", "Opérations commerciales");
            comparer("Rayon vide supprimé", true, rayons.supprimer(promo.getCode()));
            comparer("Rayons restants", 2L, rayons.nombreDeRayons());

            return bilan();
        } catch (Exception e) {
            System.out.println();
            System.out.println("  [ERREUR] " + e.getClass().getSimpleName() + " : " + e.getMessage());
            System.out.println("  Le scénario n'a pas pu aller au bout, voir la trace ci-dessous.");
            e.printStackTrace(System.out);
            return 1;
        }
    }

    private void entete(AnnotationConfigApplicationContext contexte, DataSource source,
                        SessionFactory sessionFactory) {
        String base = "indisponible";
        try (Connection connexion = source.getConnection()) {
            base = connexion.getMetaData().getURL();
        } catch (Exception e) {
            base = "connexion impossible : " + e.getMessage();
        }
        System.out.println();
        System.out.println(LIGNE);
        System.out.println(" TP 5 - Spring Framework et Hibernate dans un microservice");
        System.out.println(" Conteneur Spring, DAO generique <T> et transactions declaratives");
        System.out.println(LIGNE);
        System.out.println(" JDK          : " + System.getProperty("java.version")
                + " (" + System.getProperty("java.vendor") + ")");
        System.out.println(" Spring       : " + org.springframework.core.SpringVersion.getVersion());
        System.out.println(" Hibernate    : " + org.hibernate.Version.getVersionString());
        System.out.println(" Base         : " + base);
        System.out.println(" Configuration: " + ApplicationConfig.class.getName()
                + " (" + contexte.getBeanDefinitionCount() + " beans)");
        System.out.println(LIGNE);
        System.out.println();
    }

    private void verifierConteneur(AnnotationConfigApplicationContext contexte, ProductService produits,
                                   CategorieService rayons, SessionFactory sessionFactory,
                                   PlatformTransactionManager gestionnaire, DataSource source) throws Exception {
        verifier("Configuration ApplicationConfig enregistrée", contexte.containsBeanDefinition("applicationConfig"),
                contexte.getBeanDefinitionCount() + " bean(s) déclaré(s)");
        verifier("Fabrique de sessions Hibernate exposée", sessionFactory != null,
                sessionFactory.getClass().getSimpleName());
        comparer("Gestionnaire de transactions", "HibernateTransactionManager",
                gestionnaire.getClass().getSimpleName());
        comparer("Proxy transactionnel sur ProductService", "ProductServiceImpl",
                AopUtils.getTargetClass(produits).getSimpleName());
        comparer("Proxy transactionnel sur CategorieService", "CategorieServiceImpl",
                AopUtils.getTargetClass(rayons).getSimpleName());
        try (Connection connexion = source.getConnection()) {
            verifier("Connexion JDBC opérationnelle", connexion.isValid(2),
                    connexion.getMetaData().getDatabaseProductName() + " "
                            + connexion.getMetaData().getDatabaseProductVersion());
        }
    }

    private void purger(TransactionTemplate transaction, SessionFactory sessionFactory) {
        executerSql(transaction, sessionFactory, "DELETE FROM products");
        executerSql(transaction, sessionFactory, "DELETE FROM categories");
        executerSql(transaction, sessionFactory, "ALTER TABLE products AUTO_INCREMENT = 1");
        executerSql(transaction, sessionFactory, "ALTER TABLE categories AUTO_INCREMENT = 1");
    }

    /**
     * Les DAO utilisent {@code getCurrentSession()} : ils exigent donc une
     * transaction ouverte. Ce raccourci exécute une lecture DAO dans une
     * transaction en lecture seule gérée par Spring.
     */
    private static <T> T dansTransaction(TransactionTemplate transaction, Supplier<T> lecture) {
        return transaction.execute(statut -> lecture.get());
    }

    /**
     * Deux insertions de la même référence dans une seule transaction : la
     * contrainte d'unicité fait échouer la seconde et annule la première.
     */
    private void verifierContrainteSql(TransactionTemplate transaction, ProductDao produitDao, long avant) {
        boolean reussie;
        try {
            transaction.execute(statut -> {
                produitDao.enregistrer(new Product("SKU-900", "Première insertion", 10.00));
                produitDao.enregistrer(new Product("SKU-900", "Seconde insertion", 20.00));
                return null;
            });
            reussie = true;
        } catch (RuntimeException e) {
            reussie = false;
            System.out.println("  contrainte SQL    -> " + racine(e).getMessage());
        }
        long apres = dansTransaction(transaction, produitDao::compter);
        verifier("Contrainte d'unicité appliquée et transaction annulée", !reussie,
                "insertion en double refusée, produits=" + apres);
        comparer("Aucune ligne partielle après rollback", avant, apres);
    }

    private void executerSql(TransactionTemplate transaction, SessionFactory sessionFactory, String sql) {
        transaction.execute(statut -> sessionFactory.getCurrentSession()
                .createNativeQuery(sql).executeUpdate());
    }

    private void verifierRejet(String libelle, Runnable action, String fragmentAttendu) {
        try {
            action.run();
            echec(libelle, "exception attendue contenant \"" + fragmentAttendu + "\"", "aucune exception");
        } catch (RuntimeException e) {
            String message = racine(e).getMessage();
            boolean conforme = message != null && message.toLowerCase().contains(fragmentAttendu.toLowerCase());
            verifier(libelle, conforme, e.getClass().getSimpleName() + " : " + message);
        }
    }

    private static Throwable racine(Throwable e) {
        Throwable courant = e;
        while (courant.getCause() != null && courant.getCause() != courant) {
            courant = courant.getCause();
        }
        return courant;
    }

    private static String repartition(List<Object[]> lignes) {
        StringBuilder texte = new StringBuilder();
        for (Object[] ligne : lignes) {
            if (texte.length() > 0) {
                texte.append(", ");
            }
            texte.append(ligne[0]).append('=').append(ligne[1]);
        }
        return texte.toString();
    }

    private void etape(int numero, String titre) {
        System.out.println();
        System.out.println("[" + numero + "] " + titre);
        System.out.println(SOUS_LIGNE);
    }

    private void comparer(String libelle, Object attendu, Object obtenu) {
        boolean conforme = String.valueOf(attendu).equals(String.valueOf(obtenu));
        if (conforme) {
            verifier(libelle, true, String.valueOf(obtenu));
        } else {
            echec(libelle, String.valueOf(attendu), String.valueOf(obtenu));
        }
    }

    private void verifier(String libelle, boolean condition, String observation) {
        controles++;
        if (condition) {
            System.out.printf("  [OK]    %-58s | %s%n", libelle, observation);
        } else {
            echecs++;
            System.out.printf("  [ECHEC] %-58s | %s%n", libelle, observation);
        }
    }

    private void echec(String libelle, String attendu, String obtenu) {
        controles++;
        echecs++;
        System.out.printf("  [ECHEC] %-58s | attendu=%s obtenu=%s%n", libelle, attendu, obtenu);
    }

    private int bilan() {
        System.out.println();
        System.out.println(LIGNE);
        System.out.println(" BILAN : " + (controles - echecs) + " vérification(s) conforme(s) sur " + controles);
        System.out.println(echecs == 0
                ? " État du microservice : conteneur, DAO générique et transactions opérationnels."
                : " État du microservice : " + echecs + " anomalie(s) à corriger.");
        System.out.println(LIGNE);
        return echecs == 0 ? 0 : 1;
    }

    private static void calibrageJournalisation() {
        Level niveau = Level.parse(System.getenv().getOrDefault("LOG_LEVEL", "WARNING"));
        Logger racine = Logger.getLogger("");
        racine.setLevel(niveau);
        for (Handler handler : racine.getHandlers()) {
            handler.setLevel(niveau);
            handler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord enregistrement) {
                    return String.format("[%s] %s%n", enregistrement.getLevel(),
                            formatMessage(enregistrement));
                }
            });
        }
    }
}
