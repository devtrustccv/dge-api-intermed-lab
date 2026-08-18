package cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoVinculo;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
public class GestaoRelatorioAcompanhamentoRepository {

    private static final String SELECT_BASE = """
            SELECT
                r.id,
                r.id_oferta,
                COALESCE(NULLIF(TRIM(o.codigo_referencia), ''), c.codigo_referencia) AS codigo_referencia,
                r.id_colocacao,
                r.entidade_id,
                r.denominacao_entidade,
                r.pessoa_id,
                r.nome,
                r.data_inicio,
                r.data_fim,
                r.atividades_realizadas,
                r.dificuldades,
                r.recomendacoes,
                r.relatorio_anexo,
                r.estado,
                r.date_create,
                r.user_create,
                r.date_update,
                r.user_update
            FROM emprego_t_relatorio_acomp r
            LEFT JOIN emprego_t_colocacao_candidato c ON c.id = r.id_colocacao
            LEFT JOIN emprego_t_oferta o ON o.id = COALESCE(r.id_oferta, c.id_oferta)
            """;

    private final JdbcTemplate jdbcTemplate;

    public GestaoRelatorioAcompanhamentoRepository(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<RelatorioAcompanhamentoListaResponse> listar(RelatorioAcompanhamentoFiltro filtro) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE r.entidade_id = ? AND UPPER(COALESCE(r.estado, 'A')) IN ('A', 'ATIVO')");
        params.add(filtro.entidadeId());
        if (filtro.pessoaId() != null) {
            where.append(" AND r.pessoa_id = ?");
            params.add(filtro.pessoaId());
        }
        if (filtro.codigoReferencia() != null) {
            where.append(" AND UPPER(COALESCE(NULLIF(TRIM(o.codigo_referencia), ''), c.codigo_referencia)) = UPPER(?)");
            params.add(filtro.codigoReferencia());
        }
        if (filtro.dataInicio() != null) {
            where.append(" AND r.date_create::date >= ?");
            params.add(filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            where.append(" AND r.date_create::date <= ?");
            params.add(filtro.dataFim());
        }
        String sql = SELECT_BASE + where + " ORDER BY r.date_create DESC NULLS LAST, r.id DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RelatorioAcompanhamentoListaResponse(
                getInteger(rs, "id"),
                getLong(rs, "pessoa_id"),
                rs.getString("nome"),
                getInteger(rs, "id_oferta"),
                rs.getString("codigo_referencia"),
                rs.getObject("date_create", LocalDateTime.class),
                rs.getString("relatorio_anexo"),
                rs.getString("estado"),
                rs.getString("estado")
        ), params.toArray());
    }

    public Optional<RelatorioAcompanhamentoDetalheResponse> buscarPorId(Integer id, Integer entidadeId) {
        return jdbcTemplate.query(
                SELECT_BASE + " WHERE r.id = ? AND r.entidade_id = ?",
                this::mapDetalhe,
                id,
                entidadeId
        ).stream().findFirst();
    }

    public Optional<RelatorioAcompanhamentoVinculo> buscarVinculo(
            Integer entidadeId,
            Long pessoaId,
            String codigoReferencia
    ) {
        String sql = """
                SELECT
                    o.id AS oferta_id,
                    o.codigo_referencia,
                    c.id AS colocacao_id,
                    c.entidade_id,
                    COALESCE(NULLIF(TRIM(c.denominacao_entidade), ''), o.denominacao_entidade) AS denominacao_entidade,
                    c.pessoa_id,
                    c.nome
                FROM emprego_t_colocacao_candidato c
                INNER JOIN emprego_t_oferta o ON o.id = c.id_oferta
                WHERE c.entidade_id = ?
                  AND c.pessoa_id = ?
                  AND UPPER(o.codigo_referencia) = UPPER(?)
                  AND UPPER(COALESCE(c.tipo_oferta, o.tipo_oferta, '')) = 'OFERTA_ESTAGIO'
                  AND UPPER(COALESCE(c.estado, 'A')) IN ('A', 'ATIVO')
                ORDER BY c.id DESC
                FETCH FIRST 1 ROWS ONLY
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RelatorioAcompanhamentoVinculo(
                getInteger(rs, "oferta_id"),
                rs.getString("codigo_referencia"),
                getInteger(rs, "colocacao_id"),
                getInteger(rs, "entidade_id"),
                rs.getString("denominacao_entidade"),
                getLong(rs, "pessoa_id"),
                rs.getString("nome")
        ), entidadeId, pessoaId, codigoReferencia).stream().findFirst();
    }

    public List<RelatorioAcompanhamentoOfertaSelectResponse> listarOfertas(Integer entidadeId) {
        String sql = """
                SELECT id, codigo_referencia, titulo
                FROM emprego_t_oferta
                WHERE entidade_id = ?
                  AND UPPER(TRIM(COALESCE(tipo_oferta, ''))) = 'OFERTA_ESTAGIO'
                ORDER BY codigo_referencia ASC NULLS LAST, titulo ASC NULLS LAST, id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String codigoReferencia = rs.getString("codigo_referencia");
            String titulo = rs.getString("titulo");
            return new RelatorioAcompanhamentoOfertaSelectResponse(
                    getInteger(rs, "id"),
                    codigoReferencia,
                    titulo,
                    ofertaLabel(codigoReferencia, titulo),
                    List.of()
            );
        }, entidadeId);
    }

    public Map<Integer, List<RelatorioAcompanhamentoEstagiarioSelectResponse>> listarEstagiariosPorOferta(
            Integer entidadeId
    ) {
        String sql = """
                SELECT
                    oferta.id AS oferta_id,
                    colocacao.pessoa_id,
                    MAX(colocacao.nome) AS nome
                FROM emprego_t_oferta oferta
                INNER JOIN emprego_t_colocacao_candidato colocacao
                    ON colocacao.id_oferta = oferta.id
                WHERE oferta.entidade_id = ?
                  AND UPPER(TRIM(COALESCE(oferta.tipo_oferta, ''))) = 'OFERTA_ESTAGIO'
                  AND colocacao.pessoa_id IS NOT NULL
                  AND NULLIF(TRIM(colocacao.nome), '') IS NOT NULL
                  AND UPPER(COALESCE(colocacao.estado, 'A')) IN ('A', 'ATIVO')
                  AND EXISTS (
                      SELECT 1
                      FROM emprego_t_candidatura_oferta candidatura
                      WHERE candidatura.id_oferta = oferta.id
                        AND candidatura.pessoa_id = colocacao.pessoa_id
                  )
                GROUP BY oferta.id, colocacao.pessoa_id
                ORDER BY oferta.id, MAX(colocacao.nome), colocacao.pessoa_id
                """;
        Map<Integer, List<RelatorioAcompanhamentoEstagiarioSelectResponse>> resultado = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            resultado.computeIfAbsent(getInteger(rs, "oferta_id"), id -> new ArrayList<>())
                    .add(new RelatorioAcompanhamentoEstagiarioSelectResponse(
                            getLong(rs, "pessoa_id"),
                            rs.getString("nome")
                    ));
        }, entidadeId);
        return resultado;
    }

    public Integer inserir(
            RelatorioAcompanhamentoVinculo vinculo,
            RelatorioAcompanhamentoRequest request,
            String estado,
            String utilizador
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO emprego_t_relatorio_acomp (
                        id_oferta, id_colocacao, entidade_id, denominacao_entidade, pessoa_id, nome,
                        data_inicio, data_fim, atividades_realizadas, dificuldades, recomendacoes,
                        estado, relatorio_anexo, date_create, user_create
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setInt(1, vinculo.ofertaId());
            ps.setInt(2, vinculo.colocacaoId());
            ps.setInt(3, vinculo.entidadeId());
            ps.setString(4, vinculo.denominacaoEntidade());
            ps.setLong(5, vinculo.pessoaId());
            ps.setString(6, vinculo.estagiario());
            ps.setObject(7, request.dataInicio());
            ps.setObject(8, request.dataFim());
            ps.setString(9, request.atividadesRealizadas());
            ps.setString(10, request.dificuldades());
            ps.setString(11, request.recomendacoes());
            ps.setString(12, estado);
            ps.setString(13, request.relatorioAnexo());
            ps.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(15, utilizador);
            return ps;
        }, keyHolder);
        return generatedId(keyHolder);
    }

    public void atualizar(
            Integer id,
            RelatorioAcompanhamentoVinculo vinculo,
            RelatorioAcompanhamentoRequest request,
            String utilizador
    ) {
        jdbcTemplate.update("""
                UPDATE emprego_t_relatorio_acomp
                SET id_oferta = ?, id_colocacao = ?, entidade_id = ?, denominacao_entidade = ?,
                    pessoa_id = ?, nome = ?, data_inicio = ?, data_fim = ?, atividades_realizadas = ?,
                    dificuldades = ?, recomendacoes = ?, relatorio_anexo = ?,
                    date_update = ?, user_update = ?
                WHERE id = ?
                """,
                vinculo.ofertaId(), vinculo.colocacaoId(), vinculo.entidadeId(), vinculo.denominacaoEntidade(),
                vinculo.pessoaId(), vinculo.estagiario(), request.dataInicio(), request.dataFim(),
                request.atividadesRealizadas(), request.dificuldades(), request.recomendacoes(),
                request.relatorioAnexo(), Timestamp.valueOf(LocalDateTime.now()), utilizador, id);
    }

    public void remover(Integer id, String utilizador) {
        jdbcTemplate.update("""
                UPDATE emprego_t_relatorio_acomp
                SET estado = 'I', date_update = ?, user_update = ?
                WHERE id = ?
                """, Timestamp.valueOf(LocalDateTime.now()), utilizador, id);
    }

    private RelatorioAcompanhamentoDetalheResponse mapDetalhe(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new RelatorioAcompanhamentoDetalheResponse(
                getInteger(rs, "id"), getInteger(rs, "id_oferta"), rs.getString("codigo_referencia"),
                getInteger(rs, "id_colocacao"), getInteger(rs, "entidade_id"),
                rs.getString("denominacao_entidade"), getLong(rs, "pessoa_id"), rs.getString("nome"),
                rs.getObject("data_inicio", java.time.LocalDate.class),
                rs.getObject("data_fim", java.time.LocalDate.class),
                rs.getString("atividades_realizadas"), rs.getString("dificuldades"),
                rs.getString("recomendacoes"), rs.getString("relatorio_anexo"),
                rs.getString("estado"), rs.getString("estado"),
                rs.getObject("date_create", LocalDateTime.class), rs.getString("user_create"),
                rs.getObject("date_update", LocalDateTime.class), rs.getString("user_update")
        );
    }

    private Integer generatedId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Nao foi possivel obter o id do relatorio criado.");
        }
        return key.intValue();
    }

    private Integer getInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).intValue();
    }

    private Long getLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).longValue();
    }

    private String ofertaLabel(String codigoReferencia, String titulo) {
        boolean temCodigo = codigoReferencia != null && !codigoReferencia.trim().isEmpty();
        boolean temTitulo = titulo != null && !titulo.trim().isEmpty();
        if (!temCodigo) {
            return temTitulo ? titulo.trim() : null;
        }
        if (!temTitulo) {
            return codigoReferencia.trim();
        }
        return codigoReferencia.trim() + " - " + titulo.trim();
    }
}
