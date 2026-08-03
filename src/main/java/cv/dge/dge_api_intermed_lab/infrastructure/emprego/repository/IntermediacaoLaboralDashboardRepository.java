package cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.DashboardGrupoResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.DashboardResumoOfertaResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IntermediacaoLaboralDashboardRepository {

    private static final String STATUS_CANDIDATURA_NORMALIZADO =
            "UPPER(COALESCE(status_candidatura, ''))";
    private static final String CONDICAO_CANDIDATURA_APROVADA =
            STATUS_CANDIDATURA_NORMALIZADO + " LIKE 'APROVAD%'";

    private final JdbcTemplate jdbcTemplate;

    public IntermediacaoLaboralDashboardRepository(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Long contarOfertas(Integer entidadeId) {
        return contarComFiltroEntidade(
                "SELECT COUNT(*) FROM emprego_t_oferta",
                "SELECT COUNT(*) FROM emprego_t_oferta WHERE entidade_id = ?",
                entidadeId
        );
    }

    public Long contarCandidaturas(Integer entidadeId) {
        return contarComFiltroEntidade(
                "SELECT COUNT(*) FROM emprego_t_candidatura_oferta",
                "SELECT COUNT(*) FROM emprego_t_candidatura_oferta WHERE entidade_id = ?",
                entidadeId
        );
    }

    public BigDecimal calcularMediaVagasPorOferta(Integer entidadeId) {
        BigDecimal media = buscarDecimalComFiltroEntidade(
                """
                        SELECT COALESCE(
                            CAST(SUM(COALESCE(num_vagas, 0)) AS numeric) / NULLIF(COUNT(*), 0),
                            0
                        )
                        FROM emprego_t_oferta
                        """,
                """
                        SELECT COALESCE(
                            CAST(SUM(COALESCE(num_vagas, 0)) AS numeric) / NULLIF(COUNT(*), 0),
                            0
                        )
                        FROM emprego_t_oferta
                        WHERE entidade_id = ?
                        """,
                entidadeId
        );
        return media.setScale(2, RoundingMode.HALF_UP);
    }

    public Long contarEstagiariosSelecionados(Integer entidadeId) {
        return contarComFiltroEntidade(
                "SELECT COUNT(*) FROM emprego_t_candidatura_oferta WHERE " + CONDICAO_CANDIDATURA_APROVADA,
                "SELECT COUNT(*) FROM emprego_t_candidatura_oferta WHERE entidade_id = ? AND " + CONDICAO_CANDIDATURA_APROVADA,
                entidadeId
        );
    }

    public Long contarEstagiariosAvaliados(Integer entidadeId) {
        return contarComFiltroEntidade(
                "SELECT COUNT(*) FROM emprego_t_avaliacao_estagiario",
                """
                        SELECT COUNT(*)
                        FROM emprego_t_avaliacao_estagiario avaliacao
                        INNER JOIN emprego_t_candidatura_oferta candidatura
                            ON candidatura.id = avaliacao.candidatura_id
                        WHERE candidatura.entidade_id = ?
                        """,
                entidadeId
        );
    }

    public List<DashboardGrupoResponse> listarOfertasPorEstado(Integer entidadeId) {
        return listarAgrupamentoComFiltroEntidade(
                """
                        SELECT estado AS valor, COUNT(*) AS total
                        FROM emprego_t_oferta
                        GROUP BY estado
                        ORDER BY total DESC, estado ASC
                        """,
                """
                        SELECT estado AS valor, COUNT(*) AS total
                        FROM emprego_t_oferta
                        WHERE entidade_id = ?
                        GROUP BY estado
                        ORDER BY total DESC, estado ASC
                        """,
                entidadeId
        );
    }

    public List<DashboardGrupoResponse> listarOfertasPorTipo(Integer entidadeId) {
        return listarAgrupamentoComFiltroEntidade(
                """
                        SELECT tipo_oferta AS valor, COUNT(*) AS total
                        FROM emprego_t_oferta
                        GROUP BY tipo_oferta
                        ORDER BY total DESC, tipo_oferta ASC
                        """,
                """
                        SELECT tipo_oferta AS valor, COUNT(*) AS total
                        FROM emprego_t_oferta
                        WHERE entidade_id = ?
                        GROUP BY tipo_oferta
                        ORDER BY total DESC, tipo_oferta ASC
                        """,
                entidadeId
        );
    }

    public List<DashboardGrupoResponse> listarCandidaturasPorEstado(Integer entidadeId) {
        return listarAgrupamentoComFiltroEntidade(
                """
                        SELECT status_candidatura AS valor, COUNT(*) AS total
                        FROM emprego_t_candidatura_oferta
                        GROUP BY status_candidatura
                        ORDER BY total DESC, status_candidatura ASC
                        """,
                """
                        SELECT status_candidatura AS valor, COUNT(*) AS total
                        FROM emprego_t_candidatura_oferta
                        WHERE entidade_id = ?
                        GROUP BY status_candidatura
                        ORDER BY total DESC, status_candidatura ASC
                        """,
                entidadeId
        );
    }

    public List<DashboardResumoOfertaResponse> listarResumoOfertas(Integer entidadeId) {
        String selectResumo = """
                SELECT
                    oferta.id AS id_oferta,
                    oferta.titulo AS oferta,
                    oferta.tipo_oferta AS tipo,
                    oferta.num_vagas AS total_vagas,
                    (
                        SELECT COUNT(*)
                        FROM emprego_t_candidatura_oferta candidatura
                        WHERE candidatura.id_oferta = oferta.id
                    ) AS total_candidaturas,
                    (
                        SELECT COUNT(*)
                        FROM emprego_t_candidatura_oferta candidatura
                        WHERE candidatura.id_oferta = oferta.id
                          AND UPPER(COALESCE(candidatura.status_candidatura, '')) LIKE 'APROVAD%'
                    ) AS total_candidaturas_aprovadas,
                    (
                        SELECT COUNT(*)
                        FROM emprego_t_avaliacao_estagiario avaliacao
                        INNER JOIN emprego_t_candidatura_oferta candidatura
                            ON candidatura.id = avaliacao.candidatura_id
                        WHERE candidatura.id_oferta = oferta.id
                    ) AS total_estagiarios_avaliados
                FROM emprego_t_oferta oferta
                """;
        String ordenacao = "ORDER BY oferta.date_create DESC NULLS LAST, oferta.id DESC";

        if (entidadeId == null) {
            return listarResumoOfertas(selectResumo + ordenacao);
        }
        return listarResumoOfertas(selectResumo + "WHERE oferta.entidade_id = ? " + ordenacao, entidadeId);
    }

    private List<DashboardGrupoResponse> listarAgrupamentoComFiltroEntidade(
            String sqlTodos,
            String sqlPorEntidade,
            Integer entidadeId
    ) {
        if (entidadeId == null) {
            return listarAgrupamento(sqlTodos);
        }
        return listarAgrupamento(sqlPorEntidade, entidadeId);
    }

    private List<DashboardGrupoResponse> listarAgrupamento(String sql, Object... params) {
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new DashboardGrupoResponse(
                        rs.getString("valor"),
                        rs.getLong("total")
                ),
                params
        );
    }

    private List<DashboardResumoOfertaResponse> listarResumoOfertas(String sql, Object... params) {
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new DashboardResumoOfertaResponse(
                        rs.getInt("id_oferta"),
                        rs.getString("oferta"),
                        rs.getString("tipo"),
                        rs.getObject("total_vagas", Integer.class),
                        rs.getLong("total_candidaturas"),
                        rs.getLong("total_candidaturas_aprovadas"),
                        rs.getLong("total_estagiarios_avaliados")
                ),
                params
        );
    }

    private BigDecimal buscarDecimalComFiltroEntidade(String sqlTodos, String sqlPorEntidade, Integer entidadeId) {
        if (entidadeId == null) {
            return buscarDecimal(sqlTodos);
        }
        return buscarDecimal(sqlPorEntidade, entidadeId);
    }

    private BigDecimal buscarDecimal(String sql, Object... params) {
        BigDecimal valor = jdbcTemplate.queryForObject(sql, BigDecimal.class, params);
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private Long contarComFiltroEntidade(String sqlTodos, String sqlPorEntidade, Integer entidadeId) {
        if (entidadeId == null) {
            return contar(sqlTodos);
        }
        return contar(sqlPorEntidade, entidadeId);
    }

    private Long contar(String sql, Object... params) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class, params);
        return total == null ? 0L : total;
    }
}
