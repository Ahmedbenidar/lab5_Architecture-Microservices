package mliaedu.tp5.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mliaedu.tp5.dao.CategorieDao;
import mliaedu.tp5.dao.ProductDao;
import mliaedu.tp5.domaine.Categorie;
import mliaedu.tp5.domaine.Product;

/**
 * Implémentation des cas d'usage du catalogue.
 *
 * <p>{@code @Transactional} sur la classe : chaque méthode publique s'exécute
 * dans une transaction ouverte par Spring. Les lectures sont déclarées en
 * {@code readOnly} pour que la session ne salisse pas le contexte de
 * persistance.</p>
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final CategorieDao categorieDao;

    public ProductServiceImpl(ProductDao productDao, CategorieDao categorieDao) {
        this.productDao = productDao;
        this.categorieDao = categorieDao;
    }

    @Override
    public Product ajouter(String reference, String name, double price, String codeCategorie) {
        String ref = exigerTexte(reference, "La référence du produit");
        String libelle = exigerTexte(name, "Le nom du produit");
        BigDecimal montant = exigerPrix(price);

        if (productDao.referenceExiste(ref)) {
            throw new IllegalArgumentException("La référence " + ref + " est déjà utilisée");
        }

        Product produit = new Product(ref, libelle, montant);
        produit.setCategorie(resoudreRayon(codeCategorie));
        return productDao.enregistrer(produit);
    }

    @Override
    public int importer(String codeCategorie, List<LigneProduit> lignes) {
        if (lignes == null || lignes.isEmpty()) {
            throw new IllegalArgumentException("Le lot à importer est vide");
        }
        Categorie rayon = resoudreRayon(codeCategorie);
        List<Product> crees = new ArrayList<>();
        for (LigneProduit ligne : lignes) {
            if (ligne == null) {
                throw new IllegalArgumentException("Ligne d'import nulle");
            }
            String ref = exigerTexte(ligne.reference(), "La référence du produit");
            if (productDao.referenceExiste(ref)) {
                throw new IllegalArgumentException("La référence " + ref + " est déjà utilisée");
            }
            Product produit = new Product(ref, exigerTexte(ligne.name(), "Le nom du produit"),
                    exigerPrix(ligne.price()));
            produit.setCategorie(rayon);
            crees.add(productDao.enregistrer(produit));
        }
        return crees.size();
    }

    @Override
    public Product modifierPrix(String reference, double nouveauPrix) {
        String ref = exigerTexte(reference, "La référence du produit");
        BigDecimal montant = exigerPrix(nouveauPrix);
        Product produit = productDao.parReference(ref)
                .orElseThrow(() -> new IllegalArgumentException("Aucun produit pour la référence " + ref));
        produit.setPrice(montant);
        return productDao.mettreAJour(produit);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> parReference(String reference) {
        return reference == null || reference.isBlank()
                ? Optional.empty()
                : productDao.parReference(reference.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> catalogue() {
        return productDao.tous();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> auDessusDe(double seuil) {
        return productDao.plusChersQue(BigDecimal.valueOf(seuil).setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    @Transactional(readOnly = true)
    public double prixMoyen() {
        return productDao.prixMoyen().setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public boolean retirer(String reference) {
        String ref = exigerTexte(reference, "La référence du produit");
        Optional<Product> produit = productDao.parReference(ref);
        return produit.isPresent() && productDao.supprimer(produit.get().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long nombreDeProduits() {
        return productDao.compter();
    }

    private Categorie resoudreRayon(String codeCategorie) {
        if (codeCategorie == null || codeCategorie.isBlank()) {
            return null;
        }
        String code = codeCategorie.trim();
        return categorieDao.parCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Le rayon " + code + " n'existe pas"));
    }

    private static String exigerTexte(String valeur, String champ) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(champ + " est obligatoire");
        }
        return valeur.trim();
    }

    private static BigDecimal exigerPrix(double prix) {
        if (prix < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif : " + prix);
        }
        return BigDecimal.valueOf(prix).setScale(2, RoundingMode.HALF_UP);
    }
}
