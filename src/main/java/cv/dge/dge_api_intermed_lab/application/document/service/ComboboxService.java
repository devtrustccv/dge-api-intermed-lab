package cv.dge.dge_api_intermed_lab.application.document.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComboboxService {

    private static final String SQL_DOCUMENTOS_ATIVOS = """
            SELECT
                p.id,
                p.tipo_documento_desc
            FROM emprego_t_tipo_documento_param p
            WHERE UPPER(p.status) = 'A'
            AND p.formulario_referente = 'CIDADAO_UTENTE'
            ORDER BY p.date_create DESC, p.id DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    public ComboboxService(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarDocumentosAtivos() {
        return jdbcTemplate.query(
                SQL_DOCUMENTOS_ATIVOS,
                (rs, rowNum) -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("tipo_documento_desc", rs.getString("tipo_documento_desc"));
                    return item;
                }
        );
    }
}
