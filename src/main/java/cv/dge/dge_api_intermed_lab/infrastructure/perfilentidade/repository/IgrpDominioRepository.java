package cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoDominioResponse;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IgrpDominioRepository {

    private static final String LISTAR_POR_DOMINIO = """
            SELECT d.id,
                   d.description,
                   d.domain_type,
                   d.dominio,
                   d.ordem,
                   d.status,
                   d.valor,
                   d.env_fk
              FROM tbl_domain d
              JOIN tbl_env e ON e.id = d.env_fk
             WHERE UPPER(TRIM(d.dominio)) = UPPER(TRIM(?))
               AND LOWER(TRIM(e.dad)) = LOWER(TRIM(?))
             ORDER BY d.ordem NULLS LAST, d.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public IgrpDominioRepository(@Qualifier("igrpDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<EmpregoDominioResponse> listarPorDominio(String dominio, String dad) {
        return jdbcTemplate.query(
                LISTAR_POR_DOMINIO,
                (rs, rowNum) -> new EmpregoDominioResponse(
                        rs.getObject("id", Integer.class),
                        rs.getString("description"),
                        rs.getString("domain_type"),
                        rs.getString("dominio"),
                        rs.getObject("ordem", Integer.class),
                        rs.getString("status"),
                        rs.getString("valor"),
                        rs.getObject("env_fk", Integer.class)
                ),
                dominio,
                dad
        );
    }
}
