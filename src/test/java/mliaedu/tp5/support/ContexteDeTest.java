package mliaedu.tp5.support;

import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import mliaedu.tp5.config.ApplicationConfig;
import mliaedu.tp5.service.CategorieService;
import mliaedu.tp5.service.ProductService;

/**
 * Base commune aux tests d'intégration : démarre le conteneur Spring réel et
 * enveloppe chaque test dans une transaction annulée à la fin, pour que la
 * base reste dans son état d'origine.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfig.class)
@Transactional
public abstract class ContexteDeTest {

    @Autowired
    protected ProductService produits;

    @Autowired
    protected CategorieService rayons;

    protected static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
