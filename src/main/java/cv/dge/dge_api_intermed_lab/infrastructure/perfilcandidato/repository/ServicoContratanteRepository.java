package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoFiltro;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class ServicoContratanteRepository {

    private static final String CAMPOS_SERVICO = """
            servico.id,
            servico.contratante_id,
            servico.nome,
            servico.tipo_servico,
            servico.titulo,
            servico.descricao,
            servico.data_pretendida,
            servico.valor_previsto,
            servico.competencias_exigidas,
            servico.inicio_candidatura,
            servico.fim_candidatura,
            servico.ilha,
            servico.concelho,
            servico.zona,
            servico.telefone,
            servico.email,
            servico.anexos,
            servico.estado,
            servico.date_create,
            servico.user_create,
            servico.date_update,
            servico.user_update
            """;

    private final JdbcTemplate empregoJdbcTemplate;
    private final JdbcTemplate globalJdbcTemplate;
    private final ObjectMapper objectMapper;

    public ServicoContratanteRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("tertiaryDataSource") DataSource tertiaryDataSource,
            ObjectMapper objectMapper
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.globalJdbcTemplate = new JdbcTemplate(tertiaryDataSource);
        this.objectMapper = objectMapper;
    }

    public List<ServicoRegisto> listar(ServicoContratanteFiltro filtro) {
        List<Object> parametros = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE servico.contratante_id = ?\n");
        parametros.add(filtro.pessoaId());
        if (temTexto(filtro.tipoServico())) {
            where.append("AND servico.tipo_servico ILIKE ?\n");
            parametros.add("%" + filtro.tipoServico().trim() + "%");
        }
        if (temTexto(filtro.estado())) {
            where.append("AND UPPER(TRIM(servico.estado)) = UPPER(TRIM(?))\n");
            parametros.add(filtro.estado());
        }
        if (filtro.dataInicio() != null) {
            where.append("AND servico.date_create >= ?\n");
            parametros.add(filtro.dataInicio().atStartOfDay());
        }
        if (filtro.dataFim() != null) {
            where.append("AND servico.date_create < ?\n");
            parametros.add(filtro.dataFim().plusDays(1).atStartOfDay());
        }

        String sql = """
                SELECT
                """ + CAMPOS_SERVICO + """
                FROM emprego_t_intermediacao servico
                """ + where + """
                ORDER BY servico.date_create DESC NULLS LAST, servico.id DESC
                """;
        return empregoJdbcTemplate.query(sql, this::mapearServico, parametros.toArray());
    }

    public Optional<ServicoRegisto> buscarPorId(Integer servicoId, Long contratanteId) {
        String sql = """
                SELECT
                """ + CAMPOS_SERVICO + """
                FROM emprego_t_intermediacao servico
                WHERE servico.id = ?
                  AND servico.contratante_id = ?
                """;
        return empregoJdbcTemplate.query(sql, this::mapearServico, servicoId, contratanteId)
                .stream()
                .findFirst();
    }

    public List<ServicoCandidatoRegisto> listarParaCandidato(ServicoCandidatoFiltro filtro) {
        List<Object> parametros = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE candidatura.pessoa_id = ?\n");
        parametros.add(filtro.pessoaId());
        if (temTexto(filtro.tipoServico())) {
            where.append("AND servico.tipo_servico ILIKE ?\n");
            parametros.add("%" + filtro.tipoServico().trim() + "%");
        }
        if (temTexto(filtro.estado())) {
            where.append("AND UPPER(TRIM(servico.estado)) = UPPER(TRIM(?))\n");
            parametros.add(filtro.estado());
        }
        if (filtro.dataInicio() != null) {
            where.append("AND servico.date_create >= ?\n");
            parametros.add(filtro.dataInicio().atStartOfDay());
        }
        if (filtro.dataFim() != null) {
            where.append("AND servico.date_create < ?\n");
            parametros.add(filtro.dataFim().plusDays(1).atStartOfDay());
        }

        String sql = """
                SELECT
                """ + CAMPOS_SERVICO + """
                , candidatura.id AS candidatura_id,
                  candidatura.pessoa_id,
                  candidatura.status_candidatura,
                  candidatura.selecao_iefp,
                  candidatura.status_aceitacao_candidato
                FROM emprego_t_intermediacao servico
                INNER JOIN emprego_t_intermediacao_candidato candidatura
                    ON candidatura.id_intermediacao = servico.id
                """ + where + """
                ORDER BY servico.date_create DESC NULLS LAST,
                         candidatura.date_create DESC NULLS LAST,
                         candidatura.id DESC
                """;
        return empregoJdbcTemplate.query(sql, this::mapearServicoCandidato, parametros.toArray());
    }

    public Optional<ServicoCandidatoRegisto> buscarParaCandidato(Integer servicoId, Long pessoaId) {
        String sql = """
                SELECT
                """ + CAMPOS_SERVICO + """
                , candidatura.id AS candidatura_id,
                  candidatura.pessoa_id,
                  candidatura.status_candidatura,
                  candidatura.selecao_iefp,
                  candidatura.status_aceitacao_candidato
                FROM emprego_t_intermediacao servico
                INNER JOIN emprego_t_intermediacao_candidato candidatura
                    ON candidatura.id_intermediacao = servico.id
                WHERE servico.id = ?
                  AND candidatura.pessoa_id = ?
                ORDER BY candidatura.date_create DESC NULLS LAST, candidatura.id DESC
                """;
        return empregoJdbcTemplate.query(sql, this::mapearServicoCandidato, servicoId, pessoaId)
                .stream()
                .findFirst();
    }

    public boolean atualizarAceitacaoCandidato(
            Integer servicoId,
            Integer candidaturaId,
            Long pessoaId,
            String statusAceitacao,
            String utilizador
    ) {
        int atualizados = empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_intermediacao_candidato candidatura
                        SET status_aceitacao_candidato = ?,
                            date_update = CURRENT_TIMESTAMP,
                            user_update = ?
                        WHERE candidatura.id = ?
                          AND candidatura.id_intermediacao = ?
                          AND candidatura.pessoa_id = ?
                        """,
                statusAceitacao,
                utilizador,
                candidaturaId,
                servicoId,
                pessoaId
        );
        return atualizados == 1;
    }

    public Integer inserir(
            Long contratanteId,
            String nomeContratante,
            ServicoContratanteRequest request,
            String estado
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO emprego_t_intermediacao (
                                contratante_id,
                                nome,
                                tipo_servico,
                                titulo,
                                descricao,
                                data_pretendida,
                                valor_previsto,
                                competencias_exigidas,
                                inicio_candidatura,
                                fim_candidatura,
                                ilha,
                                concelho,
                                zona,
                                telefone,
                                email,
                                estado,
                                date_create,
                                user_create
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                            """,
                    new String[]{"id"}
            );
            statement.setLong(1, contratanteId);
            statement.setString(2, nomeContratante);
            preencherDados(statement, 3, request);
            statement.setString(16, estado);
            statement.setString(17, request.utilizador());
            return statement;
        }, keyHolder);
        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public boolean atualizar(Integer servicoId, Long contratanteId, ServicoContratanteRequest request) {
        int atualizados = empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            UPDATE emprego_t_intermediacao
                            SET tipo_servico = ?,
                                titulo = ?,
                                descricao = ?,
                                data_pretendida = ?,
                                valor_previsto = ?,
                                competencias_exigidas = ?,
                                inicio_candidatura = ?,
                                fim_candidatura = ?,
                                ilha = ?,
                                concelho = ?,
                                zona = ?,
                                telefone = ?,
                                email = ?,
                                date_update = CURRENT_TIMESTAMP,
                                user_update = ?
                            WHERE id = ?
                              AND contratante_id = ?
                            """
            );
            preencherDados(statement, 1, request);
            statement.setString(14, request.utilizador());
            statement.setInt(15, servicoId);
            statement.setLong(16, contratanteId);
            return statement;
        });
        return atualizados == 1;
    }

    public boolean atualizarAnexos(
            Integer servicoId,
            Long contratanteId,
            List<AnexoArmazenado> anexos
    ) {
        int atualizados = empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            UPDATE emprego_t_intermediacao
                            SET anexos = ?
                            WHERE id = ?
                              AND contratante_id = ?
                            """
            );
            statement.setObject(1, escreverJson(anexos), Types.OTHER);
            statement.setInt(2, servicoId);
            statement.setLong(3, contratanteId);
            return statement;
        });
        return atualizados == 1;
    }

    public boolean alterarEstado(
            Integer servicoId,
            Long contratanteId,
            String estado,
            String utilizador
    ) {
        int atualizados = empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_intermediacao
                        SET estado = ?,
                            date_update = CURRENT_TIMESTAMP,
                            user_update = ?
                        WHERE id = ?
                          AND contratante_id = ?
                        """,
                estado,
                utilizador,
                servicoId,
                contratanteId
        );
        return atualizados == 1;
    }

    public Optional<String> buscarNomePessoa(Long pessoaId) {
        return globalJdbcTemplate.query(
                "SELECT nome FROM ci_t_pessoa WHERE id = ?",
                (rs, rowNum) -> rs.getString("nome"),
                pessoaId
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
                        ORDER BY nome, id
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
                              SELECT CAST(parent.id AS VARCHAR)
                              FROM global_geografia parent
                              WHERE CAST(parent.pais AS VARCHAR) = '238'
                                AND CAST(parent.nivel_detalhe AS VARCHAR) = '2'
                                AND (
                                    CAST(parent.id AS VARCHAR) = ?
                                    OR UPPER(CAST(parent.codigo AS VARCHAR)) = UPPER(?)
                                )
                              FETCH FIRST 1 ROWS ONLY
                          )
                          AND NULLIF(TRIM(concelho.nome), '') IS NOT NULL
                        ORDER BY concelho.nome, concelho.id
                        """,
                this::mapearOpcaoGeografia,
                ilha.trim(),
                ilha.trim()
        );
    }

    public List<ConsultaVagaOpcaoResponse> listarZonas(String concelho) {
        if (!temTexto(concelho)) {
            return List.of();
        }
        return globalJdbcTemplate.query(
                """
                        SELECT CAST(zona.id AS VARCHAR) AS id,
                               CAST(zona.codigo AS VARCHAR) AS codigo,
                               zona.nome
                        FROM global_geografia zona
                        WHERE CAST(zona.pais AS VARCHAR) = '238'
                          AND CAST(zona.nivel_detalhe AS VARCHAR) = '5'
                          AND CAST(zona.concelho AS VARCHAR) = (
                              SELECT CAST(parent.id AS VARCHAR)
                              FROM global_geografia parent
                              WHERE CAST(parent.pais AS VARCHAR) = '238'
                                AND CAST(parent.nivel_detalhe AS VARCHAR) = '3'
                                AND (
                                    CAST(parent.id AS VARCHAR) = ?
                                    OR UPPER(CAST(parent.codigo AS VARCHAR)) = UPPER(?)
                                )
                              FETCH FIRST 1 ROWS ONLY
                          )
                          AND NULLIF(TRIM(zona.nome), '') IS NOT NULL
                        ORDER BY zona.nome, zona.id
                        """,
                this::mapearOpcaoGeografia,
                concelho.trim(),
                concelho.trim()
        );
    }

    public boolean existeIlha(String ilha) {
        return existeGeografia(ilha, "2");
    }

    public boolean existeConcelho(String concelho, String ilha) {
        if (!temTexto(concelho)) {
            return true;
        }
        if (!temTexto(ilha)) {
            return existeGeografia(concelho, "3");
        }
        return existeGeografiaFilha(concelho, "3", "ilha", ilha, "2");
    }

    public boolean existeZona(String zona, String concelho) {
        if (!temTexto(zona)) {
            return true;
        }
        if (!temTexto(concelho)) {
            return existeGeografia(zona, "5");
        }
        return existeGeografiaFilha(zona, "5", "concelho", concelho, "3");
    }

    public List<CandidatoRegisto> listarCandidatos(ServicoContratanteCandidatoFiltro filtro) {
        List<Object> parametros = new ArrayList<>();
        StringBuilder where = new StringBuilder("""
                WHERE candidatura.id_intermediacao = ?
                  AND servico.contratante_id = ?
                """);
        parametros.add(filtro.servicoId());
        parametros.add(filtro.contratanteId());
        if (filtro.candidatoId() != null) {
            where.append("AND candidatura.pessoa_id = ?\n");
            parametros.add(filtro.candidatoId());
        }
        if (temTexto(filtro.estado())) {
            where.append("AND UPPER(TRIM(candidatura.status_candidatura)) = UPPER(TRIM(?))\n");
            parametros.add(filtro.estado());
        }
        if (filtro.dataInicio() != null) {
            where.append("AND candidatura.data_candidatura >= ?\n");
            parametros.add(filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            where.append("AND candidatura.data_candidatura <= ?\n");
            parametros.add(filtro.dataFim());
        }

        String sql = """
                SELECT candidatura.id,
                       candidatura.pessoa_id,
                       candidatura.nome,
                       candidatura.data_candidatura,
                       candidatura.status_candidatura,
                       candidatura.selecao_iefp,
                       candidatura.status_aceitacao_candidato,
                       servico.tipo_servico,
                       servico.titulo
                FROM emprego_t_intermediacao_candidato candidatura
                INNER JOIN emprego_t_intermediacao servico
                    ON servico.id = candidatura.id_intermediacao
                """ + where + """
                ORDER BY candidatura.data_candidatura DESC NULLS LAST,
                         candidatura.date_create DESC NULLS LAST,
                         candidatura.id DESC
                """;
        return empregoJdbcTemplate.query(sql, this::mapearCandidato, parametros.toArray());
    }

    public Optional<CandidatoRegisto> buscarCandidato(
            Integer servicoId,
            Integer candidaturaId,
            Long contratanteId
    ) {
        return empregoJdbcTemplate.query(
                """
                        SELECT candidatura.id,
                               candidatura.pessoa_id,
                               candidatura.nome,
                               candidatura.data_candidatura,
                               candidatura.status_candidatura,
                               candidatura.selecao_iefp,
                               candidatura.status_aceitacao_candidato,
                               servico.tipo_servico,
                               servico.titulo
                        FROM emprego_t_intermediacao_candidato candidatura
                        INNER JOIN emprego_t_intermediacao servico
                            ON servico.id = candidatura.id_intermediacao
                        WHERE candidatura.id = ?
                          AND candidatura.id_intermediacao = ?
                          AND servico.contratante_id = ?
                        """,
                this::mapearCandidato,
                candidaturaId,
                servicoId,
                contratanteId
        ).stream().findFirst();
    }

    public List<ServicoContratanteCandidatoOpcaoResponse> listarCandidatosParaFiltro(
            Integer servicoId,
            Long contratanteId
    ) {
        return empregoJdbcTemplate.query(
                """
                        SELECT candidatura.pessoa_id, MAX(candidatura.nome) AS nome
                        FROM emprego_t_intermediacao_candidato candidatura
                        INNER JOIN emprego_t_intermediacao servico
                            ON servico.id = candidatura.id_intermediacao
                        WHERE candidatura.id_intermediacao = ?
                          AND servico.contratante_id = ?
                        GROUP BY candidatura.pessoa_id
                        ORDER BY MAX(candidatura.nome), candidatura.pessoa_id
                        """,
                (rs, rowNum) -> new ServicoContratanteCandidatoOpcaoResponse(
                        getLong(rs, "pessoa_id"),
                        rs.getString("nome")
                ),
                servicoId,
                contratanteId
        );
    }

    public boolean selecionarCandidato(
            Integer servicoId,
            Integer candidaturaId,
            Long contratanteId,
            String estado,
            String utilizador
    ) {
        int atualizados = empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_intermediacao_candidato candidatura
                        SET status_candidatura = ?,
                            date_update = CURRENT_TIMESTAMP,
                            user_update = ?
                        WHERE candidatura.id = ?
                          AND candidatura.id_intermediacao = ?
                          AND EXISTS (
                              SELECT 1
                              FROM emprego_t_intermediacao servico
                              WHERE servico.id = candidatura.id_intermediacao
                                AND servico.contratante_id = ?
                          )
                        """,
                estado,
                utilizador,
                candidaturaId,
                servicoId,
                contratanteId
        );
        return atualizados == 1;
    }

    private void preencherDados(
            PreparedStatement statement,
            int inicio,
            ServicoContratanteRequest request
    ) throws SQLException {
        statement.setString(inicio, request.tipoServico());
        statement.setString(inicio + 1, request.titulo());
        statement.setString(inicio + 2, request.descricao());
        statement.setObject(inicio + 3, request.dataPretendida());
        setBigDecimal(statement, inicio + 4, request.valorPrevisto());
        statement.setString(inicio + 5, request.competenciasExigidas());
        statement.setObject(inicio + 6, request.inicioCandidatura());
        statement.setObject(inicio + 7, request.fimCandidatura());
        statement.setString(inicio + 8, request.ilha());
        statement.setString(inicio + 9, request.concelho());
        statement.setString(inicio + 10, request.zona());
        statement.setString(inicio + 11, request.telefone());
        statement.setString(inicio + 12, request.email());
    }

    private ServicoRegisto mapearServico(ResultSet rs, int rowNum) throws SQLException {
        return new ServicoRegisto(
                rs.getInt("id"),
                getLong(rs, "contratante_id"),
                rs.getString("nome"),
                rs.getString("tipo_servico"),
                rs.getString("titulo"),
                rs.getString("descricao"),
                rs.getObject("data_pretendida", LocalDate.class),
                rs.getBigDecimal("valor_previsto"),
                rs.getString("competencias_exigidas"),
                rs.getObject("inicio_candidatura", LocalDate.class),
                rs.getObject("fim_candidatura", LocalDate.class),
                rs.getString("ilha"),
                rs.getString("concelho"),
                rs.getString("zona"),
                rs.getString("telefone"),
                rs.getString("email"),
                lerAnexos(rs.getObject("anexos")),
                rs.getString("estado"),
                rs.getObject("date_create", LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", LocalDateTime.class),
                rs.getString("user_update")
        );
    }

    private CandidatoRegisto mapearCandidato(ResultSet rs, int rowNum) throws SQLException {
        return new CandidatoRegisto(
                rs.getInt("id"),
                getLong(rs, "pessoa_id"),
                rs.getString("nome"),
                rs.getString("tipo_servico"),
                rs.getString("titulo"),
                rs.getString("status_candidatura"),
                rs.getString("selecao_iefp"),
                rs.getString("status_aceitacao_candidato"),
                rs.getObject("data_candidatura", LocalDate.class)
        );
    }

    private ServicoCandidatoRegisto mapearServicoCandidato(ResultSet rs, int rowNum) throws SQLException {
        return new ServicoCandidatoRegisto(
                mapearServico(rs, rowNum),
                rs.getInt("candidatura_id"),
                getLong(rs, "pessoa_id"),
                rs.getString("status_candidatura"),
                rs.getString("selecao_iefp"),
                rs.getString("status_aceitacao_candidato")
        );
    }

    private ConsultaVagaOpcaoResponse mapearOpcaoGeografia(ResultSet rs, int rowNum) throws SQLException {
        return new ConsultaVagaOpcaoResponse(
                converterLong(rs.getString("id")),
                rs.getString("codigo"),
                rs.getString("nome")
        );
    }

    private boolean existeGeografia(String valor, String nivel) {
        if (!temTexto(valor)) {
            return true;
        }
        Boolean existe = globalJdbcTemplate.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM global_geografia
                            WHERE CAST(pais AS VARCHAR) = '238'
                              AND CAST(nivel_detalhe AS VARCHAR) = ?
                              AND (
                                  CAST(id AS VARCHAR) = ?
                                  OR UPPER(CAST(codigo AS VARCHAR)) = UPPER(?)
                              )
                        )
                        """,
                Boolean.class,
                nivel,
                valor.trim(),
                valor.trim()
        );
        return Boolean.TRUE.equals(existe);
    }

    private boolean existeGeografiaFilha(
            String valor,
            String nivel,
            String colunaParent,
            String parent,
            String nivelParent
    ) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM global_geografia geografia
                    WHERE CAST(geografia.pais AS VARCHAR) = '238'
                      AND CAST(geografia.nivel_detalhe AS VARCHAR) = ?
                      AND (
                          CAST(geografia.id AS VARCHAR) = ?
                          OR UPPER(CAST(geografia.codigo AS VARCHAR)) = UPPER(?)
                      )
                      AND CAST(geografia.%s AS VARCHAR) = (
                          SELECT CAST(parent.id AS VARCHAR)
                          FROM global_geografia parent
                          WHERE CAST(parent.pais AS VARCHAR) = '238'
                            AND CAST(parent.nivel_detalhe AS VARCHAR) = ?
                            AND (
                                CAST(parent.id AS VARCHAR) = ?
                                OR UPPER(CAST(parent.codigo AS VARCHAR)) = UPPER(?)
                            )
                          FETCH FIRST 1 ROWS ONLY
                      )
                )
                """.formatted(colunaParent);
        Boolean existe = globalJdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                nivel,
                valor.trim(),
                valor.trim(),
                nivelParent,
                parent.trim(),
                parent.trim()
        );
        return Boolean.TRUE.equals(existe);
    }

    private List<AnexoArmazenado> lerAnexos(Object valor) {
        if (valor == null || !temTexto(valor.toString())) {
            return List.of();
        }
        try {
            JsonNode raiz = objectMapper.readTree(valor.toString());
            if (raiz.isObject() && raiz.has("documentos")) {
                raiz = raiz.get("documentos");
            }
            List<AnexoArmazenado> anexos = new ArrayList<>();
            if (raiz.isArray()) {
                raiz.forEach(item -> adicionarAnexo(anexos, item));
            } else {
                adicionarAnexo(anexos, raiz);
            }
            return List.copyOf(anexos);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private void adicionarAnexo(List<AnexoArmazenado> anexos, JsonNode item) {
        if (item == null || item.isNull()) {
            return;
        }
        if (item.isTextual()) {
            String path = item.asText();
            if (temTexto(path)) {
                anexos.add(new AnexoArmazenado(nomeDoPath(path), path));
            }
            return;
        }
        String path = primeiroTexto(item, "path", "caminho", "url", "ver_documento");
        if (!temTexto(path)) {
            return;
        }
        String nome = primeiroTexto(item, "nome", "name", "fileName", "ficheiro");
        anexos.add(new AnexoArmazenado(temTexto(nome) ? nome : nomeDoPath(path), path));
    }

    private String primeiroTexto(JsonNode item, String... campos) {
        for (String campo : campos) {
            JsonNode valor = item.get(campo);
            if (valor != null && !valor.isNull() && temTexto(valor.asText())) {
                return valor.asText().trim();
            }
        }
        return null;
    }

    private String escreverJson(List<AnexoArmazenado> anexos) throws SQLException {
        try {
            return objectMapper.writeValueAsString(anexos == null ? List.of() : anexos);
        } catch (Exception ex) {
            throw new SQLException("JSON inválido para os anexos da prestação de serviços.", ex);
        }
    }

    private void setBigDecimal(PreparedStatement statement, int indice, BigDecimal valor) throws SQLException {
        if (valor == null) {
            statement.setNull(indice, Types.NUMERIC);
        } else {
            statement.setBigDecimal(indice, valor);
        }
    }

    private Long getLong(ResultSet rs, String coluna) throws SQLException {
        Object valor = rs.getObject(coluna);
        if (valor == null) {
            return null;
        }
        return valor instanceof Number numero ? numero.longValue() : Long.valueOf(valor.toString());
    }

    private Long converterLong(String valor) {
        try {
            return temTexto(valor) ? Long.valueOf(valor.trim()) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String nomeDoPath(String path) {
        int indice = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return indice < 0 ? path : path.substring(indice + 1);
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    public record AnexoArmazenado(String nome, String path) {
    }

    public record ServicoRegisto(
            Integer servicoId,
            Long contratanteId,
            String nomeContratante,
            String tipoServico,
            String titulo,
            String descricao,
            LocalDate dataPretendida,
            BigDecimal valorPrevisto,
            String competenciasExigidas,
            LocalDate inicioCandidatura,
            LocalDate fimCandidatura,
            String ilha,
            String concelho,
            String zona,
            String telefone,
            String email,
            List<AnexoArmazenado> anexos,
            String estado,
            LocalDateTime dateCreate,
            String userCreate,
            LocalDateTime dateUpdate,
            String userUpdate
    ) {
    }

    public record CandidatoRegisto(
            Integer candidaturaId,
            Long pessoaId,
            String nome,
            String tipoServico,
            String titulo,
            String estado,
            String selecaoIefp,
            String statusAceitacaoCandidato,
            LocalDate dataCandidatura
    ) {
    }

    public record ServicoCandidatoRegisto(
            ServicoRegisto servico,
            Integer candidaturaId,
            Long pessoaId,
            String statusCandidatura,
            String selecaoIefp,
            String statusAceitacao
    ) {
    }
}
