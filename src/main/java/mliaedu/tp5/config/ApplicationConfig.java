package mliaedu.tp5.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

/**
 * Point d'entrée du conteneur : balaye les composants du microservice
 * ({@code @Service}, {@code @Repository}) et importe la configuration de
 * persistance.
 *
 * <p>Les classes {@code @Configuration} sont exclues du balayage : elles sont
 * déclarées explicitement par {@code @Import}, sinon elles seraient traitées
 * deux fois.</p>
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "mliaedu.tp5",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
@Import(PersistanceConfig.class)
public class ApplicationConfig {
}
