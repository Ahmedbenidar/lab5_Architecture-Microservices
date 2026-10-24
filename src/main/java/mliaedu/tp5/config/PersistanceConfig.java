package mliaedu.tp5.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration de la persistance : source de données, {@link SessionFactory}
 * et gestionnaire de transactions.
 *
 * <p>Les valeurs viennent de {@code classpath:persistance.properties} ; elles
 * restent surchargeables au lancement par une propriété système
 * ({@code -Ddb.url=...}) ou une variable d'environnement, ce qui évite de
 * recompiler le microservice d'un environnement à l'autre.</p>
 *
 * <p>{@code proxyBeanMethods = false} : les méthodes {@code @Bean} reçoivent
 * leurs dépendances en paramètres, aucun proxy CGLIB n'est nécessaire.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@PropertySource("classpath:persistance.properties")
public class PersistanceConfig {

    @Bean
    public DataSource dataSource(@Value("${db.driver}") String pilote,
                                 @Value("${db.url}") String url,
                                 @Value("${db.user}") String utilisateur,
                                 @Value("${db.password:}") String motDePasse) {
        DriverManagerDataSource source = new DriverManagerDataSource();
        source.setDriverClassName(pilote);
        source.setUrl(url);
        source.setUsername(utilisateur);
        source.setPassword(motDePasse);
        return source;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource,
            @Value("${hibernate.dialect:org.hibernate.dialect.MySQL8Dialect}") String dialecte,
            @Value("${hibernate.hbm2ddl.auto:update}") String generationSchema,
            @Value("${hibernate.show_sql:false}") String afficherSql,
            @Value("${hibernate.format_sql:false}") String formaterSql) {
        LocalSessionFactoryBean fabrique = new LocalSessionFactoryBean();
        fabrique.setDataSource(dataSource);
        fabrique.setPackagesToScan("mliaedu.tp5.domaine");

        Properties proprietes = new Properties();
        proprietes.put("hibernate.dialect", dialecte);
        proprietes.put("hibernate.hbm2ddl.auto", generationSchema);
        proprietes.put("hibernate.show_sql", afficherSql);
        proprietes.put("hibernate.format_sql", formaterSql);
        proprietes.put("hibernate.connection.autocommit", "false");
        fabrique.setHibernateProperties(proprietes);
        return fabrique;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager gestionnaire = new HibernateTransactionManager();
        gestionnaire.setSessionFactory(sessionFactory);
        return gestionnaire;
    }
}
