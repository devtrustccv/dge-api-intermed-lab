package cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeOfertaSelectResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GestaoAssiduidadeRepository {

    private static final String SELECT_BASE = """
            SELECT
                a.id,
                a.id_colocacao,
                cc.id_oferta AS oferta_id,
                o.titulo AS oferta,
                a.entidade_id,
                a.denominacao_entidade,
                a.pessoa_id AS estagiario_id,
                a.nome AS estagiario,
                a.data,
                a.hora_entrada,
                a.hora_saida,
                a.tipo_assiduidade,
                a.justificacao,
                a.estado,
                a.observacao,
                a.comprovativo,
                a.date_create,
                a.user_create,
                a.date_update,
                a.user_update
            FROM emprego_t_assiduidade a
            LEFT JOIN emprego_t_colocacao_candidato cc ON cc.id = a.id_colocacao
            LEFT JOIN emprego_t_oferta o ON o.id = cc.id_oferta
            """;

    private final JdbcTemplate jdbcTemplate;

    public GestaoAssiduidadeRepository(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<AssiduidadeEstagiarioListaResponse> listar(AssiduidadeEstagiarioFiltro filtro) {
        List<Object> params = new ArrayList<>();
        String sql = SELECT_BASE + construirWhere(filtro, params) + """
                ORDER BY a.data DESC NULLS LAST, a.date_create DESC NULLS LAST, a.id DESC
                """;

        return jdbcTemplate.query(sql, this::mapLista, params.toArray());
    }

    public Optional<AssiduidadeEstagiarioDetalheResponse> buscarPorId(Integer id, Integer entidadeId) {
        List<AssiduidadeEstagiarioDetalheResponse> resultados = jdbcTemplate.query(
                SELECT_BASE + """
                WHERE a.id = ?
                  AND a.entidade_id = ?
                """,
                this::mapDetalhe,
                id,
                entidadeId
        );
        return resultados.stream().findFirst();
    }

    public void validar(Integer id, Integer entidadeId, String estado, String observacao, String utilizador) {
        jdbcTemplate.update(
                """
                UPDATE emprego_t_assiduidade
                SET estado = ?,
                    observacao = ?,
                    date_update = ?,
                    user_update = ?
                WHERE id = ?
                  AND entidade_id = ?
                """,
                estado,
                observacao,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id,
                entidadeId
        );
    }

    public List<AssiduidadeEstagiarioSelectResponse> listarEstagiariosParaFiltro(Integer entidadeId) {
        return jdbcTemplate.query(
                """
                SELECT
                    a.pessoa_id AS estagiario_id,
                    COALESCE(MAX(NULLIF(TRIM(c.nome), '')), MAX(NULLIF(TRIM(a.nome), ''))) AS estagiario
                FROM emprego_t_assiduidade a
                LEFT JOIN emprego_t_candidatura_oferta c
                    ON c.pessoa_id = a.pessoa_id
                   AND c.entidade_id = a.entidade_id
                WHERE a.entidade_id = ?
                  AND a.pessoa_id IS NOT NULL
                GROUP BY a.pessoa_id
                ORDER BY estagiario ASC NULLS LAST, a.pessoa_id ASC
                """,
                (rs, rowNum) -> new AssiduidadeEstagiarioSelectResponse(
                        getLong(rs, "estagiario_id"),
                        rs.getString("estagiario")
                ),
                entidadeId
        );
    }

    public List<AssiduidadeOfertaSelectResponse> listarOfertasParaFiltro(Integer entidadeId) {
        return jdbcTemplate.query(
                """
                SELECT DISTINCT
                    o.id AS oferta_id,
                    o.titulo AS oferta
                FROM emprego_t_assiduidade a
                INNER JOIN emprego_t_colocacao_candidato cc ON cc.id = a.id_colocacao
                INNER JOIN emprego_t_oferta o ON o.id = cc.id_oferta
                WHERE a.entidade_id = ?
                  AND cc.entidade_id = ?
                  AND o.id IS NOT NULL
                  AND NULLIF(TRIM(o.titulo), '') IS NOT NULL
                ORDER BY o.titulo ASC NULLS LAST, o.id ASC
                """,
                (rs, rowNum) -> new AssiduidadeOfertaSelectResponse(
                        getInteger(rs, "oferta_id"),
                        rs.getString("oferta")
                ),
                entidadeId,
                entidadeId
        );
    }

    private String construirWhere(AssiduidadeEstagiarioFiltro filtro, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE a.entidade_id = ?");
        params.add(filtro.entidadeId());

        if (filtro.estagiarioId() != null) {
            where.append(" AND a.pessoa_id = ?");
            params.add(filtro.estagiarioId());
        }
        if (filtro.ofertaId() != null) {
            where.append(" AND cc.id_oferta = ?");
            params.add(filtro.ofertaId());
        }
        if (temTexto(filtro.tipoAssiduidade())) {
            where.append(" AND UPPER(a.tipo_assiduidade) = UPPER(?)");
            params.add(filtro.tipoAssiduidade());
        }
        if (temTexto(filtro.estado())) {
            where.append(" AND UPPER(a.estado) = UPPER(?)");
            params.add(filtro.estado());
        }
        return where + " ";
    }

    private AssiduidadeEstagiarioListaResponse mapLista(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new AssiduidadeEstagiarioListaResponse(
                getInteger(rs, "id"),
                getInteger(rs, "id_colocacao"),
                getInteger(rs, "oferta_id"),
                rs.getString("oferta"),
                getLong(rs, "estagiario_id"),
                rs.getString("estagiario"),
                rs.getString("tipo_assiduidade"),
                rs.getString("tipo_assiduidade"),
                rs.getObject("data", java.time.LocalDate.class),
                rs.getObject("hora_entrada", java.time.LocalTime.class),
                rs.getObject("hora_saida", java.time.LocalTime.class),
                null,
                rs.getString("estado"),
                rs.getString("estado"),
                null
        );
    }

    private AssiduidadeEstagiarioDetalheResponse mapDetalhe(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new AssiduidadeEstagiarioDetalheResponse(
                getInteger(rs, "id"),
                getInteger(rs, "id_colocacao"),
                getInteger(rs, "oferta_id"),
                rs.getString("oferta"),
                getInteger(rs, "entidade_id"),
                rs.getString("denominacao_entidade"),
                getLong(rs, "estagiario_id"),
                rs.getString("estagiario"),
                rs.getObject("data", java.time.LocalDate.class),
                rs.getObject("hora_entrada", java.time.LocalTime.class),
                rs.getObject("hora_saida", java.time.LocalTime.class),
                rs.getString("tipo_assiduidade"),
                rs.getString("tipo_assiduidade"),
                rs.getString("justificacao"),
                rs.getString("estado"),
                rs.getString("estado"),
                rs.getString("observacao"),
                rs.getString("comprovativo"),
                rs.getObject("date_create", LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", LocalDateTime.class),
                rs.getString("user_update")
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

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
