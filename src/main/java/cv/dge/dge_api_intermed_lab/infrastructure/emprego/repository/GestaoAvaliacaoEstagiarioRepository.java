package cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class GestaoAvaliacaoEstagiarioRepository {

    private static final String CONDICAO_OFERTA_ESTAGIO =
            "UPPER(COALESCE(c.tipo_oferta, '')) = 'OFERTA_ESTAGIO'";
    private static final String CONDICAO_ESTAGIO_ATIVO = CONDICAO_OFERTA_ESTAGIO
            + " AND UPPER(COALESCE(c.estado, 'A')) IN ('A', 'ATIVO')";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public GestaoAvaliacaoEstagiarioRepository(
            @Qualifier("primaryDataSource") DataSource dataSource,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = objectMapper;
    }

    public List<AvaliacaoEstagiarioListaResponse> listar(AvaliacaoEstagiarioFiltro filtro) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT a.id, a.pessoa_id, a.nome, a.tipo_avaliacao, a.periodo_referencia,
                       a.classificacao, a.date_create
                FROM emprego_t_avaliacao_estagiario a
                WHERE EXISTS (
                    SELECT 1 FROM emprego_t_colocacao_candidato c
                    WHERE c.id_candidatura = a.candidatura_id
                      AND c.pessoa_id = a.pessoa_id AND c.entidade_id = ? AND
                """ + CONDICAO_OFERTA_ESTAGIO + ")");
        params.add(filtro.entidadeId());
        if (filtro.pessoaId() != null) { sql.append(" AND a.pessoa_id = ?"); params.add(filtro.pessoaId()); }
        if (filtro.tipoAvaliacao() != null) { sql.append(" AND a.tipo_avaliacao = ?"); params.add(filtro.tipoAvaliacao()); }
        if (filtro.periodoReferencia() != null) { sql.append(" AND UPPER(a.periodo_referencia) LIKE UPPER(?)"); params.add("%" + filtro.periodoReferencia() + "%"); }
        if (filtro.dataInicio() != null) { sql.append(" AND a.date_create >= ?"); params.add(filtro.dataInicio().atStartOfDay()); }
        if (filtro.dataFim() != null) { sql.append(" AND a.date_create < ?"); params.add(filtro.dataFim().plusDays(1).atStartOfDay()); }
        sql.append(" ORDER BY a.date_create DESC, a.id DESC");
        return jdbcTemplate.query(sql.toString(), (rs, n) -> new AvaliacaoEstagiarioListaResponse(
                rs.getInt("id"), getLong(rs, "pessoa_id"), rs.getString("nome"), rs.getString("tipo_avaliacao"),
                rs.getString("tipo_avaliacao"), rs.getString("periodo_referencia"), decimal(rs.getString("classificacao")),
                rs.getTimestamp("date_create") == null ? null : rs.getTimestamp("date_create").toLocalDateTime()), params.toArray());
    }

    public Optional<AvaliacaoEstagiarioDetalheResponse> buscarPorId(Integer id, Integer entidadeId) {
        String sql = """
                SELECT a.* FROM emprego_t_avaliacao_estagiario a
                WHERE a.id = ? AND EXISTS (
                    SELECT 1 FROM emprego_t_colocacao_candidato c
                    WHERE c.id_candidatura = a.candidatura_id
                      AND c.pessoa_id = a.pessoa_id AND c.entidade_id = ? AND
                """ + CONDICAO_OFERTA_ESTAGIO + ")";
        return jdbcTemplate.query(sql, (rs, n) -> new AvaliacaoEstagiarioDetalheResponse(
                rs.getInt("id"), getLong(rs, "pessoa_id"), rs.getString("nome"), getInteger(rs, "candidatura_id"),
                rs.getString("tipo_avaliacao"), rs.getString("tipo_avaliacao"), rs.getString("periodo_referencia"),
                readDesempenho(rs.getObject("avaliacao_desempenho")).stream()
                        .map(item -> new AvaliacaoDesempenhoResponse(
                                item.tipoCompetencia(), null, item.avaliacao(), null))
                        .toList(), rs.getString("grau_satisfacao"),
                rs.getString("grau_satisfacao"), rs.getString("interesse_contratacao"),
                decimal(rs.getString("classificacao")), rs.getString("observacao"),
                timestamp(rs, "date_create"), rs.getString("user_create"), timestamp(rs, "date_update"),
                rs.getString("user_update")), id, entidadeId).stream().findFirst();
    }

    public Optional<AvaliacaoEstagiarioVinculo> buscarEstagiarioElegivel(Integer entidadeId, Long pessoaId) {
        String sql = """
                SELECT c.pessoa_id, c.nome, c.id_candidatura
                FROM emprego_t_colocacao_candidato c
                WHERE c.entidade_id = ? AND c.pessoa_id = ? AND
                """ + CONDICAO_ESTAGIO_ATIVO + " ORDER BY c.id DESC LIMIT 1";
        return jdbcTemplate.query(sql, (rs, n) -> new AvaliacaoEstagiarioVinculo(
                getLong(rs, "pessoa_id"), rs.getString("nome"), getInteger(rs, "id_candidatura")),
                entidadeId, pessoaId).stream().findFirst();
    }

    public List<AvaliacaoEstagiarioSelectResponse> listarEstagiarios(Integer entidadeId) {
        String sql = """
                SELECT DISTINCT c.pessoa_id, c.nome
                FROM emprego_t_colocacao_candidato c
                WHERE c.entidade_id = ? AND c.pessoa_id IS NOT NULL AND NULLIF(TRIM(c.nome), '') IS NOT NULL AND
                """ + CONDICAO_ESTAGIO_ATIVO + " ORDER BY c.nome, c.pessoa_id";
        return jdbcTemplate.query(sql, (rs, n) -> new AvaliacaoEstagiarioSelectResponse(
                getLong(rs, "pessoa_id"), rs.getString("nome")), entidadeId);
    }

    public Integer inserir(AvaliacaoEstagiarioVinculo vinculo, AvaliacaoEstagiarioRequest request, String utilizador) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO emprego_t_avaliacao_estagiario
                    (nome, pessoa_id, candidatura_id, tipo_avaliacao, periodo_referencia, avaliacao_desempenho,
                     grau_satisfacao, interesse_contratacao, classificacao, observacao, date_create, user_create)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                    """, new String[]{"id"});
            preencher(ps, vinculo, request); ps.setString(11, utilizador); return ps;
        }, keys);
        return keys.getKey().intValue();
    }

    public void atualizar(Integer id, AvaliacaoEstagiarioVinculo vinculo, AvaliacaoEstagiarioRequest request, String utilizador) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    UPDATE emprego_t_avaliacao_estagiario SET nome=?, pessoa_id=?, candidatura_id=?, tipo_avaliacao=?,
                    periodo_referencia=?, avaliacao_desempenho=?, grau_satisfacao=?, interesse_contratacao=?,
                    classificacao=?, observacao=?, date_update=CURRENT_TIMESTAMP, user_update=? WHERE id=?
                    """);
            preencher(ps, vinculo, request); ps.setString(11, utilizador); ps.setInt(12, id); return ps;
        });
    }

    private void preencher(PreparedStatement ps, AvaliacaoEstagiarioVinculo v, AvaliacaoEstagiarioRequest r) throws java.sql.SQLException {
        ps.setString(1, v.nome()); ps.setLong(2, v.pessoaId()); setInteger(ps, 3, v.candidaturaId());
        ps.setString(4, r.tipoAvaliacao()); ps.setString(5, r.periodoReferencia()); setJsonb(ps, 6, r.avaliacaoDesempenho());
        ps.setString(7, r.grauSatisfacao()); ps.setString(8, r.interesseContratacao());
        ps.setString(9, r.classificacao().stripTrailingZeros().toPlainString()); ps.setString(10, r.observacao());
    }

    private void setJsonb(PreparedStatement ps, int i, Object value) throws java.sql.SQLException {
        try { ps.setObject(i, objectMapper.writeValueAsString(value), Types.OTHER); }
        catch (Exception ex) { throw new java.sql.SQLException("JSON invalido para avaliacaoDesempenho.", ex); }
    }
    private List<AvaliacaoDesempenhoRequest> readDesempenho(Object value) {
        if (value == null) return List.of();
        try { return objectMapper.readValue(value.toString(), new TypeReference<>() {}); }
        catch (Exception ex) { return List.of(); }
    }
    private BigDecimal decimal(String value) { return value == null || value.isBlank() ? null : new BigDecimal(value); }
    private Long getLong(java.sql.ResultSet rs, String c) throws java.sql.SQLException { Object v=rs.getObject(c); return v==null?null:((Number)v).longValue(); }
    private Integer getInteger(java.sql.ResultSet rs, String c) throws java.sql.SQLException { Object v=rs.getObject(c); return v==null?null:((Number)v).intValue(); }
    private java.time.LocalDateTime timestamp(java.sql.ResultSet rs, String c) throws java.sql.SQLException { var v=rs.getTimestamp(c); return v==null?null:v.toLocalDateTime(); }
    private void setInteger(PreparedStatement ps, int i, Integer v) throws java.sql.SQLException { if(v==null) ps.setNull(i, Types.INTEGER); else ps.setInt(i,v); }
}
