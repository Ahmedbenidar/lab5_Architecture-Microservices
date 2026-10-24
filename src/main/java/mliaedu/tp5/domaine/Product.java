package mliaedu.tp5.domaine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Produit du catalogue. La référence (SKU) est l'identifiant métier unique ;
 * l'identifiant technique est généré par la base.
 */
@Entity
@Table(name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_reference", columnNames = "reference"))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", nullable = false, length = 32)
    private String reference;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    protected Product() {
    }

    public Product(String reference, String name, double price) {
        this(reference, name, BigDecimal.valueOf(price));
    }

    public Product(String reference, String name, BigDecimal price) {
        this.reference = reference;
        this.name = name;
        this.price = arrondir(price);
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public double getPriceAsDouble() {
        return price.doubleValue();
    }

    public void setPrice(BigDecimal price) {
        this.price = arrondir(price);
    }

    public void setPrice(double price) {
        setPrice(BigDecimal.valueOf(price));
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        if (this.categorie == categorie) {
            return;
        }
        if (this.categorie != null) {
            this.categorie.detacher(this);
        }
        this.categorie = categorie;
        if (categorie != null) {
            categorie.rattacher(this);
        }
    }

    public String getCodeCategorie() {
        return categorie == null ? "-" : categorie.getCode();
    }

    private static BigDecimal arrondir(BigDecimal valeur) {
        return valeur == null ? null : valeur.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Product)) {
            return false;
        }
        Product produit = (Product) autre;
        return id != null && Objects.equals(id, produit.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "Product#" + id + " [" + reference + "] " + name
                + " (" + price + " EUR, rayon " + getCodeCategorie() + ")";
    }
}
