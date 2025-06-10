package ma.ensa.ebankingver1.config;

<<<<<<< HEAD
=======

>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
<<<<<<< HEAD
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
=======
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
<<<<<<< HEAD
=======
import org.springframework.core.env.Environment;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("ma.ensa.ebankingver1.repository")
@PropertySource("classpath:database.properties")
public class DataConfig {

<<<<<<< HEAD
    @Autowired
    private Environment environment;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
=======
    private final String PROPERTY_DRIVER = "driver";
    private final String PROPERTY_URL = "url";
    private final String PROPERTY_USERNAME = "user";
    private final String PROPERTY_PASSWORD = "password";
    private final String PROPERTY_SHOW_SQL = "hibernate.show_sql";
    private final String PROPRETY_HBM2DDL = "hibernate.hbm2ddl.auto";
    private final String PROPERTY_DIALECT = "hibernate.dialect";

    @Autowired
    Environment environment;

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
        LocalContainerEntityManagerFactoryBean lfb = new LocalContainerEntityManagerFactoryBean();
        lfb.setDataSource(dataSource());
        lfb.setPackagesToScan("ma.ensa.ebankingver1.model");
        lfb.setJpaProperties(hibernateProps());
<<<<<<< HEAD
        lfb.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class);
        return lfb;
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(environment.getProperty("url"));
        ds.setUsername(environment.getProperty("user"));
        ds.setPassword(environment.getProperty("password"));
        ds.setDriverClassName(environment.getProperty("driver"));
        ds.setMaximumPoolSize(Integer.parseInt(environment.getProperty("hikari.maximumPoolSize", "10")));
        ds.setMinimumIdle(Integer.parseInt(environment.getProperty("hikari.minimumIdle", "5")));
        return ds;
    }

    @Bean
    public Properties hibernateProps() {
        Properties properties = new Properties();
        for (var key : ((org.springframework.core.env.AbstractEnvironment) environment).getPropertySources()) {
            if (key instanceof org.springframework.core.env.EnumerablePropertySource) {
                for (String name : ((org.springframework.core.env.EnumerablePropertySource<?>) key).getPropertyNames()) {
                    if (name.startsWith("hibernate.")) {
                        properties.setProperty(name, environment.getProperty(name));
                    }
                }
            }
        }
=======
        lfb.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class); // Fournisseur explicite
        return lfb;
    }
    @Bean
    DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(environment.getProperty(PROPERTY_URL));
        ds.setUsername(environment.getProperty(PROPERTY_USERNAME));
        ds.setPassword(environment.getProperty(PROPERTY_PASSWORD));
        ds.setDriverClassName(environment.getProperty(PROPERTY_DRIVER));
        return ds;
    }

    Properties hibernateProps() {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_SHOW_SQL, environment.getProperty(PROPERTY_SHOW_SQL));
        properties.setProperty(PROPRETY_HBM2DDL, environment.getProperty(PROPRETY_HBM2DDL));
        properties.setProperty(PROPERTY_DIALECT, environment.getProperty(PROPERTY_DIALECT));


>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
        return properties;
    }

    @Bean
<<<<<<< HEAD
    public JpaTransactionManager transactionManager() {
=======
    JpaTransactionManager transactionManager() {
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
<<<<<<< HEAD

    @PreDestroy
    public void closeDataSource() {
        try {
            ((HikariDataSource) dataSource()).close();
            System.out.println("✅ HikariDataSource closed");
        } catch (Exception e) {
            System.err.println("❌ Failed to close HikariDataSource: " + e.getMessage());
        }
    }
=======
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
}