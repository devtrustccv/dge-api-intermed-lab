package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AreaGeograficaProcuradaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaPorTipoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ColocacaoRecenteResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.EvolucaoCandidaturaMensalResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PerfilCandidatoDashboardRepository {

    private static final String TIPO_OFERTA_NORMALIZADO =
            "UPPER(TRIM(COALESCE(tipo_oferta, '')))";
    private static final String ESTADO_OFERTA_NORMALIZADO =
            "UPPER(TRIM(COALESCE(estado, '')))";

    private final JdbcTemplate jdbcTemplate;

    public PerfilCandidatoDashboardRepository(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Long contarColocacoes(Long pessoaId) {
        return contar(
                "SELECT COUNT(*) FROM emprego_t_colocacao_candidato WHERE pessoa_id = ?",
                pessoaId
        );
    }

    public Long contarCandidaturas(Long pessoaId) {
        return contar(
                "SELECT COUNT(*) FROM emprego_t_candidatura_oferta WHERE pessoa_id = ?",
                pessoaId
        );
    }

    public Long contarVagasAbertas(String tipoOferta) {
        String sql = """
                SELECT COUNT(*)
                FROM emprego_t_oferta
                WHERE %s = ?
                  AND %s = 'ATIVA'
                """.formatted(TIPO_OFERTA_NORMALIZADO, ESTADO_OFERTA_NORMALIZADO);
        return contar(
                sql,
                tipoOferta
        );
    }

    public List<EvolucaoCandidaturaMensalResponse> listarEvolucaoCandidaturas(Long pessoaId, Integer ano) {
        String tipoNormalizado = "UPPER(TRIM(COALESCE(candidatura.tipo_oferta, '')))";
        String sql = """
                SELECT
                    CAST(EXTRACT(MONTH FROM candidatura.date_create) AS INTEGER) AS mes,
                    COUNT(*) FILTER (WHERE %s = 'OFERTA_EMPREGO') AS total_emprego,
                    COUNT(*) FILTER (WHERE %s = 'OFERTA_ESTAGIO') AS total_estagio
                FROM emprego_t_candidatura_oferta candidatura
                WHERE candidatura.pessoa_id = ?
                  AND CAST(EXTRACT(YEAR FROM candidatura.date_create) AS INTEGER) = ?
                GROUP BY CAST(EXTRACT(MONTH FROM candidatura.date_create) AS INTEGER)
                ORDER BY mes
                """.formatted(tipoNormalizado, tipoNormalizado);
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new EvolucaoCandidaturaMensalResponse(
                        rs.getInt("mes"),
                        null,
                        rs.getLong("total_emprego"),
                        rs.getLong("total_estagio")
                ),
                pessoaId,
                ano
        );
    }

    public List<CandidaturaPorTipoResponse> listarCandidaturasPorTipo(Long pessoaId) {
        String sql = """
                SELECT %s AS tipo_oferta, COUNT(*) AS total
                FROM emprego_t_candidatura_oferta
                WHERE pessoa_id = ?
                  AND NULLIF(TRIM(COALESCE(tipo_oferta, '')), '') IS NOT NULL
                GROUP BY %s
                ORDER BY total DESC, tipo_oferta ASC
                """.formatted(TIPO_OFERTA_NORMALIZADO, TIPO_OFERTA_NORMALIZADO);
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CandidaturaPorTipoResponse(
                        rs.getString("tipo_oferta"),
                        rs.getString("tipo_oferta"),
                        rs.getLong("total")
                ),
                pessoaId
        );
    }

    public List<AreaGeograficaProcuradaResponse> listarTopAreasGeograficas() {
        return jdbcTemplate.query(
                """
                        SELECT ilha, concelho, COUNT(*) AS total
                        FROM emprego_t_oferta
                        WHERE NULLIF(TRIM(COALESCE(ilha, '')), '') IS NOT NULL
                          AND NULLIF(TRIM(COALESCE(concelho, '')), '') IS NOT NULL
                        GROUP BY ilha, concelho
                        ORDER BY total DESC, ilha ASC, concelho ASC
                        LIMIT 5
                        """,
                (rs, rowNum) -> new AreaGeograficaProcuradaResponse(
                        rs.getString("ilha"),
                        rs.getString("concelho"),
                        rs.getLong("total")
                )
        );
    }

    public List<ColocacaoRecenteResponse> listarColocacoesRecentes(Long pessoaId) {
        return jdbcTemplate.query(
                """
                        SELECT
                            colocacao.id AS id_colocacao,
                            oferta.id AS id_oferta,
                            oferta.titulo AS nome_oferta,
                            oferta.denominacao_entidade AS nome_empresa,
                            oferta.ilha,
                            oferta.concelho,
                            COALESCE(colocacao.tipo_oferta, oferta.tipo_oferta) AS tipo_oferta,
                            colocacao.date_create AS data_colocacao
                        FROM emprego_t_colocacao_candidato colocacao
                        INNER JOIN emprego_t_oferta oferta ON oferta.id = colocacao.id_oferta
                        WHERE colocacao.pessoa_id = ?
                        ORDER BY colocacao.date_create DESC NULLS LAST, colocacao.id DESC
                        LIMIT 5
                        """,
                (rs, rowNum) -> new ColocacaoRecenteResponse(
                        rs.getInt("id_colocacao"),
                        rs.getInt("id_oferta"),
                        rs.getString("nome_oferta"),
                        rs.getString("nome_empresa"),
                        rs.getString("ilha"),
                        rs.getString("concelho"),
                        rs.getString("tipo_oferta"),
                        rs.getString("tipo_oferta"),
                        toLocalDateTime(rs.getTimestamp("data_colocacao"))
                ),
                pessoaId
        );
    }

    private Long contar(String sql, Object... parametros) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class, parametros);
        return total == null ? 0L : total;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
