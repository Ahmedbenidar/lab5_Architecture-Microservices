package mliaedu.tp5.domaine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Rayon du catalogue : regroupe plusieurs produits.
 * L'association est pilotée par {@link Product#getCategorie()}.
 */
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_categorie_code", columnNames = "code"))
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "libelle", nullable = false, length = 80)
    private String libelle;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Product> produits = new ArrayList<>();

    protected Categorie() {
    }

    public Categorie(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public List<Product> getProduits() {
        return Collections.unmodifiableList(produits);
    }

    void rattacher(Product produit) {
        produits.add(produit);
    }

    void detacher(Product produit) {
        produits.remove(produit);
    }

    public int getNombreProduits() {
        return produits.size();
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Categorie)) {
            return false;
        }
        Categorie categorie = (Categorie) autre;
        return id != null && Objects.equals(id, categorie.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "Categorie#" + id + " " + code + " \"" + libelle + "\"";
    }
}
