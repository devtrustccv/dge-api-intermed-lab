package cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoOfertaSelectResponse;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GestaoAcompanhamentoRepository {

    private static final String FROM_ESTAGIARIOS_SELECIONADOS = """
            FROM emprego_t_candidatura_oferta c
            INNER JOIN emprego_t_entrevista_oferta e ON e.id_candidatura = c.id
            LEFT JOIN emprego_t_oferta o ON o.id = c.id_oferta
            LEFT JOIN emprego_t_colocacao_candidato cc
                ON cc.id_candidatura = c.id
               AND cc.pessoa_id = c.pessoa_id
               AND UPPER(COALESCE(cc.estado, 'A')) IN ('A', 'ATIVO')
            WHERE UPPER(COALESCE(c.status_candidatura, '')) = 'APROVADO'
              AND UPPER(COALESCE(e.estado, '')) = 'REALIZADO'
              AND UPPER(COALESCE(e.parecer_entrevista, '')) = 'APROVAR'
            """;

    private final JdbcTemplate jdbcTemplate;

    public GestaoAcompanhamentoRepository(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<AcompanhamentoEstagiarioListaResponse> listarEstagiariosSelecionados(
            AcompanhamentoEstagiarioFiltro filtro
    ) {
        List<Object> params = new ArrayList<>();
        String whereFiltro = construirWhereFiltro(filtro, params);
        String sql = """
                SELECT *
                FROM (
                    SELECT DISTINCT ON (c.id)
                        c.id AS candidatura_id,
                        c.pessoa_id AS estagiario_id,
                        c.nome AS estagiario,
                        c.id_oferta AS oferta_id,
                        o.titulo AS oferta,
                        e.id AS entrevista_id,
                        e.parecer_entrevista,
                        e.estado AS estado_entrevista,
                        cc.id AS colocacao_id
                """ + FROM_ESTAGIARIOS_SELECIONADOS + whereFiltro + """
                    ORDER BY c.id, e.date_update DESC NULLS LAST, e.date_create DESC NULLS LAST, e.id DESC, cc.id DESC
                ) dados
                ORDER BY dados.estagiario ASC NULLS LAST, dados.oferta ASC NULLS LAST, dados.candidatura_id DESC
                """;

        return jdbcTemplate.query(sql, this::mapLista, params.toArray());
    }

    public List<AcompanhamentoEstagiarioSelectResponse> listarEstagiariosSelecionadosParaFiltro() {
        String sql = """
                SELECT DISTINCT
                    c.pessoa_id AS estagiario_id,
                    c.nome AS estagiario
                """ + FROM_ESTAGIARIOS_SELECIONADOS + """
                  AND c.pessoa_id IS NOT NULL
                  AND NULLIF(TRIM(c.nome), '') IS NOT NULL
                ORDER BY c.nome ASC NULLS LAST, c.pessoa_id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AcompanhamentoEstagiarioSelectResponse(
                getLong(rs, "estagiario_id"),
                rs.getString("estagiario")
        ));
    }

    public List<AcompanhamentoOfertaSelectResponse> listarOfertasComEstagiariosSelecionados() {
        String sql = """
                SELECT DISTINCT
                    o.id AS oferta_id,
                    o.titulo AS oferta
                """ + FROM_ESTAGIARIOS_SELECIONADOS + """
                  AND o.id IS NOT NULL
                  AND NULLIF(TRIM(o.titulo), '') IS NOT NULL
                ORDER BY o.titulo ASC NULLS LAST, o.id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AcompanhamentoOfertaSelectResponse(
                getInteger(rs, "oferta_id"),
                rs.getString("oferta")
        ));
    }

    private String construirWhereFiltro(AcompanhamentoEstagiarioFiltro filtro, List<Object> params) {
        StringBuilder where = new StringBuilder();
        if (filtro == null) {
            return where.toString();
        }
        if (filtro.estagiarioId() != null) {
            where.append(" AND c.pessoa_id = ?");
            params.add(filtro.estagiarioId());
        }
        if (filtro.ofertaId() != null) {
            where.append(" AND c.id_oferta = ?");
            params.add(filtro.ofertaId());
        }
        return where.toString();
    }

    private AcompanhamentoEstagiarioListaResponse mapLista(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        Integer colocacaoId = getInteger(rs, "colocacao_id");
        return new AcompanhamentoEstagiarioListaResponse(
                getInteger(rs, "candidatura_id"),
                getLong(rs, "estagiario_id"),
                rs.getString("estagiario"),
                getInteger(rs, "oferta_id"),
                rs.getString("oferta"),
                getInteger(rs, "entrevista_id"),
                rs.getString("parecer_entrevista"),
                rs.getString("parecer_entrevista"),
                rs.getString("estado_entrevista"),
                rs.getString("estado_entrevista"),
                colocacaoId,
                colocacaoId != null
        );
    }

    private Long getLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }

    private Integer getInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(value.toString());
    }
}
