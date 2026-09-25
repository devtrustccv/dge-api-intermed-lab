package cv.dge.dge_api_intermed_lab.configurations;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class IgrpDataSourceConfig {

    private static final String IGRP_DATABASE_NAME = "db_igrp_dge";

    @Bean(name = "igrpDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.igrp")
    public DataSourceProperties igrpDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "igrpDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.igrp.hikari")
    public DataSource igrpDataSource(
            @Qualifier("igrpDataSourceProperties") DataSourceProperties properties,
            @Qualifier("primaryDataSourceProperties") DataSourceProperties primaryProperties
    ) {
        completarComConfiguracaoPrincipal(properties, primaryProperties);
        return properties.initializeDataSourceBuilder().build();
    }

    private void completarComConfiguracaoPrincipal(
            DataSourceProperties properties,
            DataSourceProperties primaryProperties
    ) {
        if (!StringUtils.hasText(properties.getUrl())) {
            properties.setUrl(criarUrlIgrp(primaryProperties.getUrl()));
        }
        if (!StringUtils.hasText(properties.getUsername())) {
            properties.setUsername(primaryProperties.getUsername());
        }
        if (properties.getPassword() == null) {
            properties.setPassword(primaryProperties.getPassword());
        }
        if (!StringUtils.hasText(properties.getDriverClassName())) {
            properties.setDriverClassName(primaryProperties.getDriverClassName());
        }
    }

    private String criarUrlIgrp(String primaryUrl) {
        if (!StringUtils.hasText(primaryUrl)) {
            throw new IllegalStateException(
                    "Não foi possível configurar a ligação à base db_igrp_dge: "
                            + "defina spring.datasource.igrp.url ou spring.datasource.url."
            );
        }

        int inicioParametros = primaryUrl.indexOf('?');
        String urlSemParametros = inicioParametros >= 0
                ? primaryUrl.substring(0, inicioParametros)
                : primaryUrl;
        String parametros = inicioParametros >= 0
                ? primaryUrl.substring(inicioParametros)
                : "";
        int ultimoSeparador = urlSemParametros.lastIndexOf('/');
        int fimProtocolo = urlSemParametros.indexOf("://");
        if (fimProtocolo < 0 || ultimoSeparador <= fimProtocolo + 2) {
            throw new IllegalStateException(
                    "A propriedade spring.datasource.url não contém uma URL JDBC válida para criar "
                            + "a ligação à base db_igrp_dge. Valor recebido: \"" + primaryUrl + "\"."
            );
        }
        return urlSemParametros.substring(0, ultimoSeparador + 1)
                + IGRP_DATABASE_NAME
                + parametros;
    }
}
