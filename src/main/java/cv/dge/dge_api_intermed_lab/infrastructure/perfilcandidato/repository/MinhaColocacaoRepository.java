package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MinhaColocacaoRepository {

    private static final String CAMPOS_COLOCACAO = """
            colocacao.id AS colocacao_id,
            colocacao.id_oferta AS oferta_id,
            COALESCE(colocacao.tipo_oferta, oferta.tipo_oferta) AS tipo_oferta,
            oferta.titulo,
            COALESCE(colocacao.codigo_referencia, oferta.codigo_referencia) AS codigo_referencia,
            colocacao.data_inicio_previsto,
            colocacao.data_fim_previsto,
            colocacao.tipo_contrato,
            colocacao.duracao_contrato,
            colocacao.descricao,
            colocacao.estado,
            colocacao.date_create AS data_colocacao,
            colocacao.contrato_path
            """;

    private final JdbcTemplate empregoJdbcTemplate;

    public MinhaColocacaoRepository(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
    }

    public List<ColocacaoRegisto> listar(Long pessoaId) {
        String sql = """
                SELECT
                """ + CAMPOS_COLOCACAO + """
                FROM emprego_t_colocacao_candidato colocacao
                LEFT JOIN emprego_t_oferta oferta ON oferta.id = colocacao.id_oferta
                WHERE colocacao.pessoa_id = ?
                ORDER BY colocacao.date_create DESC NULLS LAST, colocacao.id DESC
                """;

        return empregoJdbcTemplate.query(sql, this::mapColocacao, pessoaId);
    }

    public Optional<ColocacaoRegisto> buscarPorId(Integer colocacaoId, Long pessoaId) {
        String sql = """
                SELECT
                """ + CAMPOS_COLOCACAO + """
                FROM emprego_t_colocacao_candidato colocacao
                LEFT JOIN emprego_t_oferta oferta ON oferta.id = colocacao.id_oferta
                WHERE colocacao.id = ?
                  AND colocacao.pessoa_id = ?
                """;

        return empregoJdbcTemplate.query(sql, this::mapColocacao, colocacaoId, pessoaId)
                .stream()
                .findFirst();
    }

    private ColocacaoRegisto mapColocacao(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ColocacaoRegisto(
                rs.getInt("colocacao_id"),
                rs.getObject("oferta_id", Integer.class),
                rs.getString("tipo_oferta"),
                rs.getString("titulo"),
                rs.getString("codigo_referencia"),
                rs.getObject("data_inicio_previsto", LocalDate.class),
                rs.getObject("data_fim_previsto", LocalDate.class),
                rs.getString("tipo_contrato"),
                rs.getObject("duracao_contrato", Integer.class),
                rs.getString("descricao"),
                rs.getString("estado"),
                rs.getObject("data_colocacao", LocalDateTime.class),
                rs.getString("contrato_path")
        );
    }

    public record ColocacaoRegisto(
            Integer colocacaoId,
            Integer ofertaId,
            String tipoOferta,
            String titulo,
            String codigoReferencia,
            LocalDate dataInicioPrevisto,
            LocalDate dataFimPrevisto,
            String tipoContrato,
            Integer duracaoContrato,
            String descricao,
            String estado,
            LocalDateTime dataColocacao,
            String contratoPath
    ) {
    }
}
