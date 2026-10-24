package mliaedu.tp5.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mliaedu.tp5.dao.CategorieDao;
import mliaedu.tp5.domaine.Categorie;

@Service
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private final CategorieDao categorieDao;

    public CategorieServiceImpl(CategorieDao categorieDao) {
        this.categorieDao = categorieDao;
    }

    @Override
    public Categorie creer(String code, String libelle) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Le code du rayon est obligatoire");
        }
        if (libelle == null || libelle.isBlank()) {
            throw new IllegalArgumentException("Le libellé du rayon est obligatoire");
        }
        String codeNormalise = code.trim().toUpperCase();
        if (categorieDao.parCode(codeNormalise).isPresent()) {
            throw new IllegalArgumentException("Le rayon " + codeNormalise + " existe déjà");
        }
        return categorieDao.enregistrer(new Categorie(codeNormalise, libelle.trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categorie> parCode(String code) {
        return code == null || code.isBlank() ? Optional.empty() : categorieDao.parCode(code.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categorie> inventaire() {
        return categorieDao.avecProduits();
    }

    @Override
    public boolean supprimer(String code) {
        Categorie rayon = categorieDao.parCode(code == null ? "" : code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Rayon inconnu : " + code));
        long produits = categorieDao.nombreDeProduits(rayon.getId().intValue());
        if (produits > 0) {
            throw new IllegalStateException("Le rayon " + rayon.getCode()
                    + " contient encore " + produits + " produit(s)");
        }
        return categorieDao.supprimer(rayon.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long nombreDeRayons() {
        return categorieDao.compter();
    }
}
