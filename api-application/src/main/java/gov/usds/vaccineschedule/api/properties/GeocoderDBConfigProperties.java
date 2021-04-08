package gov.usds.vaccineschedule.api.properties;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;


/**
 * Created by nickrobison on 3/30/21
 */
@EnableTransactionManagement
@Configuration
@ConditionalOnProperty("vs.geocoder.db.config")
@EnableJpaRepositories(
        basePackages = "gov.usds.vaccineschedule.api.services.geocoder",
        entityManagerFactoryRef = "tigerEntityManagerFactory",
        transactionManagerRef = "tigerTransactionManager"
)
public class GeocoderDBConfigProperties {

    private final Environment env;


    public GeocoderDBConfigProperties(Environment environment) {
        this.env = environment;
    }

    @Bean(name = "tigerEntityManager")
    public EntityManager tigerEntityManager(@Qualifier("tigerEntityManagerFactory") EntityManagerFactory factory) {
        return factory.createEntityManager();
    }


    @Bean(name = "tigerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tigerEntityManagerFactory(@Qualifier("tigerDataSource") DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);

        final HibernateJpaVendorAdapter vendorAdapter
                = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setPackagesToScan("gov.usds.vaccineschedule.api.services.geocoder");
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto",
                env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect",
                env.getProperty("hibernate.dialect"));
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.default_schema", "tiger");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "tigerDataSource")
    @ConfigurationProperties("vs.geocoder.db.config")
    public DataSource tigerDataSource(@Qualifier("tigerDataProperties") DataSourceProperties dataProperties) {
        return dataProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "tigerDataProperties")
    @ConfigurationProperties(prefix = "vs.geocoder.db")
    public DataSourceProperties tigerDataProperties() {
        return new DataSourceProperties();
    }


    @Bean(name = "tigerTransactionManager")
    public PlatformTransactionManager tigerTransactionManager(@Qualifier("tigerEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}
