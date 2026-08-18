package cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoVinculo;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoOfertaSelectResponse;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ColocacaoCandidatoRepository {

    private static final String CAMPOS_DETALHE = """
            id,
            id_oferta,
            tipo_oferta,
            codigo_referencia,
            entidade_id,
            denominacao_entidade,
            pessoa_id,
            nome,
            id_candidatura,
            data_inicio_previsto,
            data_fim_previsto,
            tipo_contrato,
            duracao_contrato,
            descricao,
            contrato_path,
            estado,
            registado_cefp,
            date_create,
            user_create,
            date_update,
            user_update
            """;

    private final JdbcTemplate jdbcTemplate;

    public ColocacaoCandidatoRepository(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<ColocacaoCandidatoListaResponse> listar(ColocacaoCandidatoFiltro filtro) {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(filtro, params, true);
        String sql = """
                SELECT
                    id,
                    tipo_oferta,
                    codigo_referencia,
                    pessoa_id,
                    nome,
                    tipo_contrato,
                    data_inicio_previsto,
                    date_create
                FROM emprego_t_colocacao_candidato
                """ + where + """
                ORDER BY date_create DESC NULLS LAST, id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ColocacaoCandidatoListaResponse(
                rs.getInt("id"),
                rs.getString("tipo_oferta"),
                rs.getString("tipo_oferta"),
                rs.getString("codigo_referencia"),
                getLong(rs, "pessoa_id"),
                rs.getString("nome"),
                rs.getString("tipo_contrato"),
                rs.getString("tipo_contrato"),
                rs.getObject("data_inicio_previsto", java.time.LocalDate.class),
                rs.getObject("date_create", LocalDateTime.class)
        ), params.toArray());
    }

    public Optional<ColocacaoCandidatoResponse> buscarPorId(Integer id) {
        String sql = "SELECT " + CAMPOS_DETALHE + " FROM emprego_t_colocacao_candidato WHERE id = ?";
        List<ColocacaoCandidatoResponse> resultados = jdbcTemplate.query(sql, this::mapDetalhe, id);
        return resultados.stream().findFirst();
    }

    public List<ColocacaoOfertaSelectResponse> listarOfertasPorTipoEEntidade(String tipoOferta, Integer entidadeId) {
        return jdbcTemplate.query(
                """
                        SELECT
                            id,
                            codigo_referencia,
                            titulo,
                            tipo_oferta,
                            entidade_id,
                            denominacao_entidade
                        FROM emprego_t_oferta
                        WHERE UPPER(tipo_oferta) = UPPER(?)
                          AND entidade_id = ?
                        ORDER BY codigo_referencia ASC NULLS LAST, titulo ASC NULLS LAST, id ASC
                        """,
                (rs, rowNum) -> {
                    String codigoReferencia = rs.getString("codigo_referencia");
                    String titulo = rs.getString("titulo");
                    return new ColocacaoOfertaSelectResponse(
                            getInteger(rs, "id"),
                            codigoReferencia,
                            titulo,
                            ofertaLabel(codigoReferencia, titulo),
                            rs.getString("tipo_oferta"),
                            rs.getString("tipo_oferta"),
                            getInteger(rs, "entidade_id"),
                            rs.getString("denominacao_entidade")
                    );
                },
                tipoOferta,
                entidadeId
        );
    }

    public List<ColocacaoCandidatoSelectResponse> listarCandidatosPorOferta(Integer ofertaId) {
        return jdbcTemplate.query(
                """
                SELECT
                    c.pessoa_id,
                    c.nome,
                    c.id AS candidatura_id,
                    o.id AS oferta_id,
                    o.codigo_referencia
                FROM emprego_t_candidatura_oferta c
                INNER JOIN emprego_t_oferta o ON o.id = c.id_oferta
                WHERE c.id_oferta = ?
                ORDER BY c.nome ASC NULLS LAST, c.id ASC
                """,
                (rs, rowNum) -> new ColocacaoCandidatoSelectResponse(
                        getLong(rs, "pessoa_id"),
                        rs.getString("nome"),
                        getInteger(rs, "candidatura_id"),
                        getInteger(rs, "oferta_id"),
                        rs.getString("codigo_referencia")
                ),
                ofertaId
        );
    }

    public Optional<ColocacaoCandidatoVinculo> buscarVinculoCandidatura(
            Integer ofertaId,
            Long pessoaId
    ) {
        String sql = """
                SELECT
                    c.id AS candidatura_id,
                    c.pessoa_id,
                    c.nome,
                    o.id AS oferta_id,
                    o.tipo_oferta,
                    o.codigo_referencia,
                    o.entidade_id,
                    o.denominacao_entidade
                FROM emprego_t_candidatura_oferta c
                INNER JOIN emprego_t_oferta o ON o.id = c.id_oferta
                WHERE c.id_oferta = ?
                  AND c.pessoa_id = ?
                ORDER BY c.id DESC
                FETCH FIRST 1 ROWS ONLY
                """;
        List<ColocacaoCandidatoVinculo> resultados = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ColocacaoCandidatoVinculo(
                        getInteger(rs, "candidatura_id"),
                        getLong(rs, "pessoa_id"),
                        rs.getString("nome"),
                        getInteger(rs, "oferta_id"),
                        rs.getString("tipo_oferta"),
                        rs.getString("codigo_referencia"),
                        getInteger(rs, "entidade_id"),
                        rs.getString("denominacao_entidade")
                ),
                ofertaId,
                pessoaId
        );
        return resultados.stream().findFirst();
    }

    public Integer inserir(
            ColocacaoCandidatoRequest request,
            ColocacaoCandidatoVinculo vinculo,
            String estado,
            Boolean registadoCefp,
            String utilizador
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime agora = LocalDateTime.now();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                            INSERT INTO emprego_t_colocacao_candidato (
                                id_oferta,
                                tipo_oferta,
                                codigo_referencia,
                                entidade_id,
                                denominacao_entidade,
                                pessoa_id,
                                nome,
                                id_candidatura,
                                data_inicio_previsto,
                                data_fim_previsto,
                                tipo_contrato,
                                duracao_contrato,
                                descricao,
                                contrato_path,
                                estado,
                                registado_cefp,
                                date_create,
                                user_create
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            ps.setInt(1, vinculo.ofertaId());
            ps.setString(2, request.tipoOferta());
            ps.setString(3, vinculo.codigoReferencia());
            setInteger(ps, 4, vinculo.entidadeId());
            ps.setString(5, vinculo.denominacaoEntidade());
            setLong(ps, 6, vinculo.pessoaId());
            ps.setString(7, vinculo.nome());
            setInteger(ps, 8, vinculo.candidaturaId());
            ps.setObject(9, request.dataInicioPrevisto());
            ps.setObject(10, request.dataFimPrevisto());
            ps.setString(11, request.tipoContrato());
            setInteger(ps, 12, request.duracaoContrato());
            ps.setString(13, request.descricao());
            ps.setString(14, request.contratoPath());
            ps.setString(15, estado);
            ps.setBoolean(16, Boolean.TRUE.equals(registadoCefp));
            ps.setTimestamp(17, Timestamp.valueOf(agora));
            ps.setString(18, utilizador);
            return ps;
        }, keyHolder);

        return generatedId(keyHolder);
    }

    public void atualizar(Integer id, ColocacaoCandidatoRequest request, ColocacaoCandidatoVinculo vinculo, String utilizador) {
        jdbcTemplate.update(
                """
                        UPDATE emprego_t_colocacao_candidato
                        SET id_oferta = ?,
                            tipo_oferta = ?,
                            codigo_referencia = ?,
                            entidade_id = ?,
                            denominacao_entidade = ?,
                            pessoa_id = ?,
                            nome = ?,
                            id_candidatura = ?,
                            data_inicio_previsto = ?,
                            data_fim_previsto = ?,
                            tipo_contrato = ?,
                            duracao_contrato = ?,
                            descricao = ?,
                            contrato_path = ?,
                            date_update = ?,
                            user_update = ?
                        WHERE id = ?
                        """,
                vinculo.ofertaId(),
                request.tipoOferta(),
                vinculo.codigoReferencia(),
                vinculo.entidadeId(),
                vinculo.denominacaoEntidade(),
                vinculo.pessoaId(),
                vinculo.nome(),
                vinculo.candidaturaId(),
                request.dataInicioPrevisto(),
                request.dataFimPrevisto(),
                request.tipoContrato(),
                request.duracaoContrato(),
                request.descricao(),
                request.contratoPath(),
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    public void remover(Integer id, String estado, String utilizador) {
        jdbcTemplate.update(
                """
                        UPDATE emprego_t_colocacao_candidato
                        SET estado = ?,
                            date_update = ?,
                            user_update = ?
                        WHERE id = ?
                        """,
                estado,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    private String construirWhere(ColocacaoCandidatoFiltro filtro, List<Object> params, boolean apenasAtivos) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (apenasAtivos) {
            where.append(" AND UPPER(COALESCE(estado, 'A')) IN ('A', 'ATIVO')");
        }

        adicionarFiltroTexto(where, params, "tipo_oferta", filtro.tipoOferta());
        adicionarFiltroTexto(where, params, "codigo_referencia", filtro.codigoReferencia());
        adicionarFiltroLong(where, params, "pessoa_id", filtro.pessoaId());
        adicionarFiltroTexto(where, params, "tipo_contrato", filtro.tipoContrato());
        adicionarFiltroNumero(where, params, "entidade_id", filtro.entidadeId());

        if (filtro.dataInicioPrevisto() != null) {
            where.append(" AND data_inicio_previsto = ?");
            params.add(filtro.dataInicioPrevisto());
        }
        if (filtro.dataRegistoInicio() != null) {
            where.append(" AND date_create::date >= ?");
            params.add(filtro.dataRegistoInicio());
        }
        if (filtro.dataRegistoFim() != null) {
            where.append(" AND date_create::date <= ?");
            params.add(filtro.dataRegistoFim());
        }

        return where + " ";
    }

    private void adicionarFiltroTexto(StringBuilder where, List<Object> params, String coluna, String valor) {
        if (!temTexto(valor)) {
            return;
        }
        where.append(" AND UPPER(").append(coluna).append(") = UPPER(?)");
        params.add(valor.trim());
    }

    private void adicionarFiltroNumero(StringBuilder where, List<Object> params, String coluna, Integer valor) {
        if (valor == null) {
            return;
        }
        where.append(" AND ").append(coluna).append(" = ?");
        params.add(valor);
    }

    private void adicionarFiltroLong(StringBuilder where, List<Object> params, String coluna, Long valor) {
        if (valor == null) {
            return;
        }
        where.append(" AND ").append(coluna).append(" = ?");
        params.add(valor);
    }

    private ColocacaoCandidatoResponse mapDetalhe(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ColocacaoCandidatoResponse(
                rs.getInt("id"),
                getInteger(rs, "id_oferta"),
                rs.getString("tipo_oferta"),
                rs.getString("tipo_oferta"),
                rs.getString("codigo_referencia"),
                getInteger(rs, "entidade_id"),
                rs.getString("denominacao_entidade"),
                getLong(rs, "pessoa_id"),
                rs.getString("nome"),
                getInteger(rs, "id_candidatura"),
                rs.getString("tipo_contrato"),
                rs.getString("tipo_contrato"),
                getInteger(rs, "duracao_contrato"),
                rs.getObject("data_inicio_previsto", java.time.LocalDate.class),
                rs.getObject("data_fim_previsto", java.time.LocalDate.class),
                rs.getString("descricao"),
                rs.getString("contrato_path"),
                rs.getString("estado"),
                rs.getString("estado"),
                rs.getObject("registado_cefp", Boolean.class),
                rs.getObject("date_create", LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", LocalDateTime.class),
                rs.getString("user_update")
        );
    }

    private Integer generatedId(KeyHolder keyHolder) {
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            Number key = keyHolder.getKey();
            return key == null ? null : key.intValue();
        }
        Object id = keys.get("id");
        if (id instanceof Number number) {
            return number.intValue();
        }
        return id == null ? null : Integer.valueOf(id.toString());
    }

    private void setInteger(PreparedStatement ps, int index, Integer value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
            return;
        }
        ps.setInt(index, value);
    }

    private void setLong(PreparedStatement ps, int index, Long value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
            return;
        }
        ps.setLong(index, value);
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

    private String ofertaLabel(String codigoReferencia, String titulo) {
        if (!temTexto(codigoReferencia)) {
            return titulo;
        }
        if (!temTexto(titulo)) {
            return codigoReferencia;
        }
        return codigoReferencia + " - " + titulo;
    }
}
