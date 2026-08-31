package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcaoResponse;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ConfiguracaoAlertaOfertaRepository {

    private static final String CAMPOS_DETALHE = """
            alerta.id,
            alerta.pessoa_id,
            alerta.tipo_oferta,
            alerta.ilha,
            alerta.concelho,
            alerta.entidade_id,
            alerta.habilitacao_literaria,
            alerta.nivel_qualificacao,
            alerta.estado,
            alerta.date_create,
            alerta.user_create,
            alerta.date_update,
            alerta.user_update
            """;

    private final JdbcTemplate empregoJdbcTemplate;
    private final JdbcTemplate globalJdbcTemplate;

    public ConfiguracaoAlertaOfertaRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("tertiaryDataSource") DataSource tertiaryDataSource
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.globalJdbcTemplate = new JdbcTemplate(tertiaryDataSource);
    }

    public List<AlertaOfertaListaRegisto> listar(Long pessoaId) {
        String sql = """
                SELECT alerta.id,
                       alerta.tipo_oferta,
                       alerta.habilitacao_literaria,
                       alerta.nivel_qualificacao,
                       alerta.estado,
                       alerta.date_create
                FROM emprego_t_alerta_config alerta
                WHERE alerta.pessoa_id = ?
                ORDER BY alerta.date_create DESC NULLS LAST, alerta.id DESC
                """;

        return empregoJdbcTemplate.query(sql, this::mapearLista, pessoaId);
    }

    public Optional<AlertaOfertaDetalheRegisto> buscarPorId(Integer alertaId, Long pessoaId) {
        String sql = """
                SELECT
                """ + CAMPOS_DETALHE + """
                FROM emprego_t_alerta_config alerta
                WHERE alerta.id = ?
                  AND alerta.pessoa_id = ?
                """;

        return empregoJdbcTemplate.query(sql, this::mapearDetalhe, alertaId, pessoaId)
                .stream()
                .findFirst();
    }

    public Integer inserir(Long pessoaId, AlertaOfertaRequest request, String estado) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO emprego_t_alerta_config (
                                pessoa_id,
                                tipo_oferta,
                                ilha,
                                concelho,
                                entidade_id,
                                habilitacao_literaria,
                                nivel_qualificacao,
                                estado,
                                date_create,
                                user_create
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                            """,
                    new String[]{"id"}
            );
            statement.setLong(1, pessoaId);
            statement.setString(2, request.tipoOferta());
            statement.setString(3, request.ilha());
            statement.setString(4, request.concelho());
            setInteger(statement, 5, request.entidadeId());
            statement.setString(6, request.habilitacaoLiteraria());
            statement.setString(7, request.nivelQualificacao());
            statement.setString(8, estado);
            statement.setString(9, request.utilizador());
            return statement;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public boolean atualizar(Integer alertaId, Long pessoaId, AlertaOfertaRequest request) {
        int atualizados = empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            UPDATE emprego_t_alerta_config
                            SET tipo_oferta = ?,
                                ilha = ?,
                                concelho = ?,
                                entidade_id = ?,
                                habilitacao_literaria = ?,
                                nivel_qualificacao = ?,
                                date_update = CURRENT_TIMESTAMP,
                                user_update = ?
                            WHERE id = ?
                              AND pessoa_id = ?
                            """
            );
            statement.setString(1, request.tipoOferta());
            statement.setString(2, request.ilha());
            statement.setString(3, request.concelho());
            setInteger(statement, 4, request.entidadeId());
            statement.setString(5, request.habilitacaoLiteraria());
            statement.setString(6, request.nivelQualificacao());
            statement.setString(7, request.utilizador());
            statement.setInt(8, alertaId);
            statement.setLong(9, pessoaId);
            return statement;
        });
        return atualizados == 1;
    }

    public Optional<String> buscarNomePessoa(Long pessoaId) {
        return globalJdbcTemplate.query(
                """
                        SELECT nome
                        FROM ci_t_pessoa
                        WHERE id = ?
                        """,
                (rs, rowNum) -> rs.getString("nome"),
                pessoaId
        ).stream().filter(this::temTexto).findFirst();
    }

    public Optional<String> buscarDenominacaoEntidade(Integer entidadeId) {
        if (entidadeId == null) {
            return Optional.empty();
        }
        return globalJdbcTemplate.query(
                """
                        SELECT denominacao
                        FROM ci_t_entidade
                        WHERE id = ?
                        """,
                (rs, rowNum) -> rs.getString("denominacao"),
                entidadeId
        ).stream().filter(this::temTexto).findFirst();
    }

    public List<ConsultaVagaOpcaoResponse> listarIlhas() {
        return globalJdbcTemplate.query(
                """
                        SELECT CAST(id AS VARCHAR) AS id,
                               CAST(codigo AS VARCHAR) AS codigo,
                               nome
                        FROM global_geografia
                        WHERE CAST(pais AS VARCHAR) = '238'
                          AND CAST(nivel_detalhe AS VARCHAR) = '2'
                          AND NULLIF(TRIM(nome), '') IS NOT NULL
                        ORDER BY nome ASC, id ASC
                        """,
                this::mapearOpcaoGeografia
        );
    }

    public List<ConsultaVagaOpcaoResponse> listarConcelhos(String ilha) {
        if (!temTexto(ilha)) {
            return List.of();
        }
        return globalJdbcTemplate.query(
                """
                        SELECT CAST(concelho.id AS VARCHAR) AS id,
                               CAST(concelho.codigo AS VARCHAR) AS codigo,
                               concelho.nome
                        FROM global_geografia concelho
                        WHERE CAST(concelho.pais AS VARCHAR) = '238'
                          AND CAST(concelho.nivel_detalhe AS VARCHAR) = '3'
                          AND CAST(concelho.ilha AS VARCHAR) = (
                              SELECT CAST(ilha.id AS VARCHAR)
                              FROM global_geografia ilha
                              WHERE CAST(ilha.pais AS VARCHAR) = '238'
                                AND CAST(ilha.nivel_detalhe AS VARCHAR) = '2'
                                AND (
                                    CAST(ilha.id AS VARCHAR) = ?
                                    OR UPPER(CAST(ilha.codigo AS VARCHAR)) = UPPER(?)
                                )
                              FETCH FIRST 1 ROWS ONLY
                          )
                          AND NULLIF(TRIM(concelho.nome), '') IS NOT NULL
                        ORDER BY concelho.nome ASC, concelho.id ASC
                        """,
                this::mapearOpcaoGeografia,
                ilha.trim(),
                ilha.trim()
        );
    }

    public List<ConsultaVagaOpcaoResponse> listarEntidades() {
        return globalJdbcTemplate.query(
                """
                        SELECT id, denominacao
                        FROM ci_t_entidade
                        WHERE id IS NOT NULL
                          AND NULLIF(TRIM(denominacao), '') IS NOT NULL
                        ORDER BY denominacao ASC, id ASC
                        """,
                (rs, rowNum) -> new ConsultaVagaOpcaoResponse(
                        getLong(rs, "id"),
                        null,
                        rs.getString("denominacao")
                )
        );
    }

    public boolean existeIlha(String ilha) {
        return existeGeografia(ilha, "2", null);
    }

    public boolean existeConcelho(String concelho, String ilha) {
        return existeGeografia(concelho, "3", ilha);
    }

    public boolean existeEntidade(Integer entidadeId) {
        if (entidadeId == null) {
            return true;
        }
        Boolean existe = globalJdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM ci_t_entidade WHERE id = ?)",
                Boolean.class,
                entidadeId
        );
        return Boolean.TRUE.equals(existe);
    }

    private boolean existeGeografia(String valor, String nivel, String ilha) {
        if (!temTexto(valor)) {
            return true;
        }
        StringBuilder sql = new StringBuilder("""
                SELECT EXISTS (
                    SELECT 1
                    FROM global_geografia geografia
                    WHERE CAST(geografia.pais AS VARCHAR) = '238'
                      AND CAST(geografia.nivel_detalhe AS VARCHAR) = ?
                      AND (
                          CAST(geografia.id AS VARCHAR) = ?
                          OR UPPER(CAST(geografia.codigo AS VARCHAR)) = UPPER(?)
                      )
                """);
        if (temTexto(ilha)) {
            sql.append("""
                      AND CAST(geografia.ilha AS VARCHAR) = (
                          SELECT CAST(ilha.id AS VARCHAR)
                          FROM global_geografia ilha
                          WHERE CAST(ilha.pais AS VARCHAR) = '238'
                            AND CAST(ilha.nivel_detalhe AS VARCHAR) = '2'
                            AND (
                                CAST(ilha.id AS VARCHAR) = ?
                                OR UPPER(CAST(ilha.codigo AS VARCHAR)) = UPPER(?)
                            )
                          FETCH FIRST 1 ROWS ONLY
                      )
                    """);
        }
        sql.append(")");

        String valorLimpo = valor.trim();
        Boolean existe = temTexto(ilha)
                ? globalJdbcTemplate.queryForObject(
                        sql.toString(),
                        Boolean.class,
                        nivel,
                        valorLimpo,
                        valorLimpo,
                        ilha.trim(),
                        ilha.trim()
                )
                : globalJdbcTemplate.queryForObject(
                        sql.toString(),
                        Boolean.class,
                        nivel,
                        valorLimpo,
                        valorLimpo
                );
        return Boolean.TRUE.equals(existe);
    }

    private AlertaOfertaListaRegisto mapearLista(ResultSet rs, int rowNum) throws SQLException {
        return new AlertaOfertaListaRegisto(
                rs.getInt("id"),
                rs.getString("tipo_oferta"),
                rs.getString("habilitacao_literaria"),
                rs.getString("nivel_qualificacao"),
                rs.getString("estado"),
                rs.getObject("date_create", LocalDateTime.class)
        );
    }

    private AlertaOfertaDetalheRegisto mapearDetalhe(ResultSet rs, int rowNum) throws SQLException {
        return new AlertaOfertaDetalheRegisto(
                rs.getInt("id"),
                getLong(rs, "pessoa_id"),
                rs.getString("tipo_oferta"),
                rs.getString("ilha"),
                rs.getString("concelho"),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("habilitacao_literaria"),
                rs.getString("nivel_qualificacao"),
                rs.getString("estado"),
                rs.getObject("date_create", LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", LocalDateTime.class),
                rs.getString("user_update")
        );
    }

    private ConsultaVagaOpcaoResponse mapearOpcaoGeografia(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString("id");
        return new ConsultaVagaOpcaoResponse(
                converterLong(id),
                rs.getString("codigo"),
                rs.getString("nome")
        );
    }

    private void setInteger(PreparedStatement statement, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            statement.setNull(indice, Types.INTEGER);
        } else {
            statement.setInt(indice, valor);
        }
    }

    private Long getLong(ResultSet rs, String coluna) throws SQLException {
        Object valor = rs.getObject(coluna);
        return valor == null ? null : ((Number) valor).longValue();
    }

    private Long converterLong(String valor) {
        try {
            return temTexto(valor) ? Long.valueOf(valor.trim()) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    public record AlertaOfertaListaRegisto(
            Integer alertaId,
            String tipoOferta,
            String habilitacaoLiteraria,
            String nivelQualificacao,
            String estado,
            LocalDateTime dataConfiguracao
    ) {
    }

    public record AlertaOfertaDetalheRegisto(
            Integer alertaId,
            Long pessoaId,
            String tipoOferta,
            String ilha,
            String concelho,
            Integer entidadeId,
            String habilitacaoLiteraria,
            String nivelQualificacao,
            String estado,
            LocalDateTime dateCreate,
            String userCreate,
            LocalDateTime dateUpdate,
            String userUpdate
    ) {
    }
}
