package cv.dge.dge_api_intermed_lab.configurations;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

class IgrpDataSourceConfigTest {

    private final IgrpDataSourceConfig config = new IgrpDataSourceConfig();

    @Test
    void deveUsarServidorECredenciaisPrincipaisQuandoConfiguracaoIgrpNaoExiste() {
        DataSourceProperties igrp = new DataSourceProperties();
        DataSourceProperties primary = propriedades(
                "jdbc:postgresql://database.prod:5432/db_dge_emprego?sslmode=require",
                "utilizador",
                "segredo"
        );

        try (HikariDataSource dataSource = (HikariDataSource) config.igrpDataSource(igrp, primary)) {
            assertThat(dataSource.getJdbcUrl())
                    .isEqualTo("jdbc:postgresql://database.prod:5432/db_igrp_dge?sslmode=require");
            assertThat(dataSource.getUsername()).isEqualTo("utilizador");
            assertThat(dataSource.getPassword()).isEqualTo("segredo");
            assertThat(dataSource.getDriverClassName()).isEqualTo("org.postgresql.Driver");
        }
    }

    @Test
    void deveManterConfiguracaoIgrpExplicitaQuandoFornecida() {
        DataSourceProperties igrp = propriedades(
                "jdbc:postgresql://igrp.prod:5432/db_igrp_dge",
                "igrp_user",
                "igrp_password"
        );
        DataSourceProperties primary = propriedades(
                "jdbc:postgresql://database.prod:5432/db_dge_emprego",
                "utilizador",
                "segredo"
        );

        try (HikariDataSource dataSource = (HikariDataSource) config.igrpDataSource(igrp, primary)) {
            assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:postgresql://igrp.prod:5432/db_igrp_dge");
            assertThat(dataSource.getUsername()).isEqualTo("igrp_user");
            assertThat(dataSource.getPassword()).isEqualTo("igrp_password");
        }
    }

    private DataSourceProperties propriedades(String url, String username, String password) {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl(url);
        properties.setUsername(username);
        properties.setPassword(password);
        properties.setDriverClassName("org.postgresql.Driver");
        return properties;
    }
}
