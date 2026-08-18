package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PerfilCandidatoCertificadoRepository {

    private final JdbcTemplate empregoJdbcTemplate;
    private final JdbcTemplate globalJdbcTemplate;

    public PerfilCandidatoCertificadoRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("tertiaryDataSource") DataSource tertiaryDataSource
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.globalJdbcTemplate = new JdbcTemplate(tertiaryDataSource);
    }

    public Optional<FonteEmprego> buscarFonteEmprego(Integer colocacaoId, Long pessoaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT
                            colocacao.id AS colocacao_id,
                            CAST(colocacao.pessoa_id AS BIGINT) AS pessoa_id,
                            colocacao.id_candidatura AS candidatura_id,
                            colocacao.nome,
                            candidatura.habilitacao_academica,
                            colocacao.denominacao_entidade AS nome_entidade,
                            colocacao.data_inicio_previsto AS data_inicio,
                            colocacao.data_fim_previsto AS data_fim,
                            avaliacao.classificacao AS classificacao_final
                        FROM emprego_t_colocacao_candidato colocacao
                        LEFT JOIN emprego_t_oferta oferta
                            ON oferta.id = colocacao.id_oferta
                        LEFT JOIN emprego_t_candidatura_oferta candidatura
                            ON candidatura.id = colocacao.id_candidatura
                        LEFT JOIN LATERAL (
                            SELECT avaliacao_estagiario.classificacao
                            FROM emprego_t_avaliacao_estagiario avaliacao_estagiario
                            WHERE avaliacao_estagiario.candidatura_id = colocacao.id_candidatura
                              AND UPPER(TRIM(COALESCE(avaliacao_estagiario.tipo_avaliacao, ''))) = 'FINAL'
                            ORDER BY
                                COALESCE(
                                    avaliacao_estagiario.date_update,
                                    avaliacao_estagiario.date_create
                                ) DESC NULLS LAST,
                                avaliacao_estagiario.id DESC
                            FETCH FIRST 1 ROWS ONLY
                        ) avaliacao ON TRUE
                        WHERE colocacao.id = ?
                          AND CAST(colocacao.pessoa_id AS BIGINT) = ?
                          AND UPPER(COALESCE(
                              NULLIF(TRIM(colocacao.tipo_oferta), ''),
                              NULLIF(TRIM(oferta.tipo_oferta), ''),
                              ''
                          ))
                              = 'OFERTA_ESTAGIO'
                        """,
                (rs, rowNum) -> new FonteEmprego(
                        rs.getInt("colocacao_id"),
                        getLong(rs, "pessoa_id"),
                        getInteger(rs, "candidatura_id"),
                        rs.getString("nome"),
                        rs.getString("habilitacao_academica"),
                        rs.getString("nome_entidade"),
                        rs.getObject("data_inicio", LocalDate.class),
                        rs.getObject("data_fim", LocalDate.class),
                        decimal(rs.getString("classificacao_final"))
                ),
                colocacaoId,
                pessoaId
        ).stream().findFirst();
    }

    public Optional<FontePessoa> buscarFontePessoa(Long pessoaId) {
        return globalJdbcTemplate.query(
                """
                        SELECT naturalidade, data_nasc, num_documento
                        FROM ci_t_pessoa
                        WHERE id = ?
                        """,
                (rs, rowNum) -> new FontePessoa(
                        rs.getString("naturalidade"),
                        rs.getObject("data_nasc", LocalDate.class),
                        rs.getString("num_documento")
                ),
                pessoaId
        ).stream().findFirst();
    }

    public Optional<CertificadoEmitido> buscarEmitido(Integer colocacaoId, Long pessoaId) {
        return empregoJdbcTemplate.query(
                sqlCertificado("WHERE colocacao_id = ? AND pessoa_id = ?"),
                this::mapCertificado,
                colocacaoId,
                pessoaId
        ).stream().findFirst();
    }

    public Optional<CertificadoEmitido> buscarEmitidoPorCodigo(String codigoContraprova) {
        return empregoJdbcTemplate.query(
                sqlCertificado("WHERE UPPER(codigo_contraprova) = UPPER(?)"),
                this::mapCertificado,
                codigoContraprova
        ).stream().findFirst();
    }

    public Integer inserir(CertificadoEmitido certificado) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO emprego_t_certificado_estagio (
                                colocacao_id,
                                pessoa_id,
                                candidatura_id,
                                nome,
                                naturalidade,
                                data_nascimento,
                                num_documento,
                                habilitacao_academica,
                                nome_entidade,
                                data_inicio,
                                data_fim,
                                classificacao_final,
                                assinatura,
                                codigo_contraprova,
                                data_emissao,
                                user_create
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            statement.setInt(1, certificado.colocacaoId());
            statement.setLong(2, certificado.pessoaId());
            setInteger(statement, 3, certificado.candidaturaId());
            statement.setString(4, certificado.nome());
            statement.setString(5, certificado.naturalidade());
            setLocalDate(statement, 6, certificado.dataNascimento());
            statement.setString(7, certificado.numeroDocumento());
            statement.setString(8, certificado.habilitacaoAcademica());
            statement.setString(9, certificado.nomeEntidade());
            setLocalDate(statement, 10, certificado.dataInicio());
            setLocalDate(statement, 11, certificado.dataFim());
            statement.setBigDecimal(12, certificado.classificacaoFinal());
            statement.setString(13, certificado.assinatura());
            statement.setString(14, certificado.codigoContraprova());
            statement.setObject(15, certificado.dataEmissao());
            statement.setString(16, certificado.utilizadorEmissao());
            return statement;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("Não foi possível concluir a emissão do certificado.");
        }
        return id.intValue();
    }

    private String sqlCertificado(String condicao) {
        return """
                SELECT
                    id,
                    colocacao_id,
                    pessoa_id,
                    candidatura_id,
                    nome,
                    naturalidade,
                    data_nascimento,
                    num_documento,
                    habilitacao_academica,
                    nome_entidade,
                    data_inicio,
                    data_fim,
                    classificacao_final,
                    assinatura,
                    codigo_contraprova,
                    data_emissao,
                    user_create
                FROM emprego_t_certificado_estagio
                """ + condicao;
    }

    private CertificadoEmitido mapCertificado(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        java.sql.Timestamp dataEmissao = rs.getTimestamp("data_emissao");
        return new CertificadoEmitido(
                rs.getInt("id"),
                rs.getInt("colocacao_id"),
                getLong(rs, "pessoa_id"),
                getInteger(rs, "candidatura_id"),
                rs.getString("nome"),
                rs.getString("naturalidade"),
                rs.getObject("data_nascimento", LocalDate.class),
                rs.getString("num_documento"),
                rs.getString("habilitacao_academica"),
                rs.getString("nome_entidade"),
                rs.getObject("data_inicio", LocalDate.class),
                rs.getObject("data_fim", LocalDate.class),
                rs.getBigDecimal("classificacao_final"),
                rs.getString("assinatura"),
                rs.getString("codigo_contraprova"),
                dataEmissao == null ? null : dataEmissao.toLocalDateTime(),
                rs.getString("user_create")
        );
    }

    private BigDecimal decimal(String valor) {
        return valor == null || valor.isBlank() ? null : new BigDecimal(valor.trim());
    }

    private Long getLong(java.sql.ResultSet rs, String coluna) throws java.sql.SQLException {
        Object valor = rs.getObject(coluna);
        return valor == null ? null : ((Number) valor).longValue();
    }

    private Integer getInteger(java.sql.ResultSet rs, String coluna) throws java.sql.SQLException {
        Object valor = rs.getObject(coluna);
        return valor == null ? null : ((Number) valor).intValue();
    }

    private void setInteger(PreparedStatement statement, int indice, Integer valor) throws java.sql.SQLException {
        if (valor == null) {
            statement.setNull(indice, Types.INTEGER);
        } else {
            statement.setInt(indice, valor);
        }
    }

    private void setLocalDate(PreparedStatement statement, int indice, LocalDate valor) throws java.sql.SQLException {
        if (valor == null) {
            statement.setNull(indice, Types.DATE);
        } else {
            statement.setObject(indice, valor);
        }
    }

    public record FonteEmprego(
            Integer colocacaoId,
            Long pessoaId,
            Integer candidaturaId,
            String nome,
            String habilitacaoAcademica,
            String nomeEntidade,
            LocalDate dataInicio,
            LocalDate dataFim,
            BigDecimal classificacaoFinal
    ) {
    }

    public record FontePessoa(
            String naturalidade,
            LocalDate dataNascimento,
            String numeroDocumento
    ) {
    }

    public record CertificadoEmitido(
            Integer id,
            Integer colocacaoId,
            Long pessoaId,
            Integer candidaturaId,
            String nome,
            String naturalidade,
            LocalDate dataNascimento,
            String numeroDocumento,
            String habilitacaoAcademica,
            String nomeEntidade,
            LocalDate dataInicio,
            LocalDate dataFim,
            BigDecimal classificacaoFinal,
            String assinatura,
            String codigoContraprova,
            LocalDateTime dataEmissao,
            String utilizadorEmissao
    ) {
    }
}
