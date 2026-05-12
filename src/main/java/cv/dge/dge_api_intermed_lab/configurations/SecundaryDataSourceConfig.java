package cv.dge.dge_api_intermed_lab.configurations;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "cv.dge.dge_api_intermed_lab.infrastructure.secundary.repository",
        entityManagerFactoryRef = "secundaryEntityManagerFactory",
        transactionManagerRef = "secundaryTransactionManager"
)
public class SecundaryDataSourceConfig {

    @Bean(name = "secundaryDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.secundary")
    public DataSourceProperties secundaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "secundaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secundary.hikari")
    public DataSource secundaryDataSource(
            @Qualifier("secundaryDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "secundaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secundaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("secundaryDataSource") DataSource dataSource
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "none");

        return builder
                .dataSource(dataSource)
                .packages("cv.dge.dge_api_intermed_lab.infrastructure.secundary")
                .persistenceUnit("secundary")
                .properties(props)
                .build();
    }

    @Bean(name = "secundaryTransactionManager")
    public PlatformTransactionManager secundaryTransactionManager(
            @Qualifier("secundaryEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
