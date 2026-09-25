package cv.dge.dge_api_intermed_lab.configurations;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgrpDataSourceConfig {

    @Bean(name = "igrpDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.igrp")
    public DataSourceProperties igrpDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "igrpDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.igrp.hikari")
    public DataSource igrpDataSource(
            @Qualifier("igrpDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }
}
