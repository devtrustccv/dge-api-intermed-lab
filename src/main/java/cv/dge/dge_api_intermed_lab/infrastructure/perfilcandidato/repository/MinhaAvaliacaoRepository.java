package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MinhaAvaliacaoRepository {

    private final JdbcTemplate empregoJdbcTemplate;
    private final ObjectMapper objectMapper;

    public MinhaAvaliacaoRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            ObjectMapper objectMapper
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.objectMapper = objectMapper;
    }

    public List<AvaliacaoListaRegisto> listar(Long pessoaId) {
        String sql = """
                SELECT avaliacao.id AS avaliacao_id,
                       avaliacao.tipo_avaliacao,
                       avaliacao.periodo_referencia,
                       avaliacao.classificacao,
                       avaliacao.date_create AS data_registo
                FROM emprego_t_avaliacao_estagiario avaliacao
                WHERE avaliacao.pessoa_id = ?
                ORDER BY avaliacao.date_create DESC NULLS LAST, avaliacao.id DESC
                """;

        return empregoJdbcTemplate.query(sql, this::mapearLista, pessoaId);
    }

    public Optional<AvaliacaoDetalheRegisto> buscarPorId(Integer avaliacaoId, Long pessoaId) {
        String sql = """
                SELECT avaliacao.id AS avaliacao_id,
                       avaliacao.tipo_avaliacao,
                       avaliacao.periodo_referencia,
                       avaliacao.avaliacao_desempenho,
                       avaliacao.grau_satisfacao,
                       avaliacao.interesse_contratacao,
                       avaliacao.classificacao,
                       avaliacao.observacao,
                       avaliacao.date_create AS data_registo
                FROM emprego_t_avaliacao_estagiario avaliacao
                WHERE avaliacao.id = ?
                  AND avaliacao.pessoa_id = ?
                """;

        return empregoJdbcTemplate.query(sql, this::mapearDetalhe, avaliacaoId, pessoaId)
                .stream()
                .findFirst();
    }

    private AvaliacaoListaRegisto mapearLista(ResultSet rs, int rowNum) throws SQLException {
        return new AvaliacaoListaRegisto(
                rs.getInt("avaliacao_id"),
                rs.getString("tipo_avaliacao"),
                rs.getString("periodo_referencia"),
                lerDecimal(rs, "classificacao"),
                rs.getObject("data_registo", LocalDateTime.class)
        );
    }

    private AvaliacaoDetalheRegisto mapearDetalhe(ResultSet rs, int rowNum) throws SQLException {
        return new AvaliacaoDetalheRegisto(
                rs.getInt("avaliacao_id"),
                rs.getString("tipo_avaliacao"),
                rs.getString("periodo_referencia"),
                lerAvaliacaoDesempenho(rs.getObject("avaliacao_desempenho")),
                rs.getString("grau_satisfacao"),
                rs.getString("interesse_contratacao"),
                lerDecimal(rs, "classificacao"),
                rs.getString("observacao"),
                rs.getObject("data_registo", LocalDateTime.class)
        );
    }

    private List<AvaliacaoDesempenhoRegisto> lerAvaliacaoDesempenho(Object valor) throws SQLException {
        if (valor == null || valor.toString().isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(valor.toString(), new TypeReference<>() { });
        } catch (Exception ex) {
            throw new SQLException("O conteúdo de avaliacao_desempenho não possui um JSON válido.", ex);
        }
    }

    private BigDecimal lerDecimal(ResultSet rs, String coluna) throws SQLException {
        String valor = rs.getString(coluna);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException ex) {
            throw new SQLException("O campo " + coluna + " não possui um valor numérico válido.", ex);
        }
    }

    public record AvaliacaoListaRegisto(
            Integer avaliacaoId,
            String tipoAvaliacao,
            String periodoReferencia,
            BigDecimal classificacao,
            LocalDateTime dataRegisto
    ) {
    }

    public record AvaliacaoDetalheRegisto(
            Integer avaliacaoId,
            String tipoAvaliacao,
            String periodoReferencia,
            List<AvaliacaoDesempenhoRegisto> avaliacaoDesempenho,
            String grauSatisfacao,
            String interesseContratacao,
            BigDecimal classificacao,
            String observacao,
            LocalDateTime dataRegisto
    ) {
    }

    public record AvaliacaoDesempenhoRegisto(
            String tipoCompetencia,
            String avaliacao
    ) {
    }
}
