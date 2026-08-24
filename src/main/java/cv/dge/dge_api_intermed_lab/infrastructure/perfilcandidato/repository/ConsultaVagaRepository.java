package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcaoResponse;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
public class ConsultaVagaRepository {

    private static final String TIPO_OFERTA_NORMALIZADO = """
            CASE UPPER(TRIM(COALESCE(o.tipo_oferta, '')))
                WHEN 'EMPREGO' THEN 'OFERTA_EMPREGO'
                WHEN 'ESTAGIO' THEN 'OFERTA_ESTAGIO'
                WHEN 'ESTÁGIO' THEN 'OFERTA_ESTAGIO'
                WHEN 'ESTAGIO_PROFISSIONAL' THEN 'OFERTA_ESTAGIO'
                ELSE UPPER(TRIM(COALESCE(o.tipo_oferta, '')))
            END
            """;
    private static final String ESTADO_OFERTA_NORMALIZADO = """
            CASE UPPER(TRIM(COALESCE(o.estado, '')))
                WHEN 'A' THEN 'ATIVA'
                WHEN 'ATIVO' THEN 'ATIVA'
                WHEN 'F' THEN 'FECHADA'
                WHEN 'FECHADO' THEN 'FECHADA'
                ELSE UPPER(TRIM(COALESCE(o.estado, '')))
            END
            """;

    private final JdbcTemplate empregoJdbcTemplate;
    private final ObjectMapper objectMapper;

    public ConsultaVagaRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            ObjectMapper objectMapper
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.objectMapper = objectMapper;
    }

    public List<OfertaResumo> listar(ConsultaVagaFiltro filtro) {
        List<Object> parametros = new ArrayList<>();
        String where = construirWhere(filtro, parametros);
        String sql = """
                SELECT
                    o.id,
                    o.titulo,
                    o.tipo_oferta,
                    o.ilha,
                    o.concelho,
                    o.num_vagas,
                    o.entidade_id,
                    o.denominacao_entidade,
                    o.codigo_referencia,
                    o.estado,
                    o.data_inicio_candidatura,
                    o.data_fim_candidatura,
                    EXISTS (
                        SELECT 1
                        FROM emprego_t_candidatura_oferta candidatura
                        WHERE candidatura.id_oferta = o.id
                          AND candidatura.pessoa_id = ?
                    ) AS ja_candidatado
                FROM emprego_t_oferta o
                """ + where + """
                ORDER BY o.data_fim_candidatura ASC NULLS LAST, o.date_create DESC NULLS LAST, o.id DESC
                """;

        List<Object> todosParametros = new ArrayList<>();
        todosParametros.add(filtro.pessoaId());
        todosParametros.addAll(parametros);
        return empregoJdbcTemplate.query(sql, this::mapResumo, todosParametros.toArray());
    }

    public Optional<OfertaDetalhe> buscarOferta(Integer ofertaId, Long pessoaId) {
        String sql = """
                SELECT
                    o.id,
                    o.codigo_referencia,
                    o.tipo_oferta,
                    o.titulo,
                    o.descricao,
                    o.data_inicio_candidatura,
                    o.data_fim_candidatura,
                    o.data_inicio_previsto,
                    o.duracao_contrato,
                    o.regime_contrato,
                    o.entidade_id,
                    o.denominacao_entidade,
                    o.habilitacao_minima,
                    o.nivel_qualificacao,
                    o.num_vagas,
                    o.habilitacao_maxima,
                    o.conhecimento_linguistico,
                    o.competencias_valorizadas,
                    o.hora_inicio,
                    o.hora_fim,
                    o.dias_semana,
                    o.cursos_area_formacao,
                    o.experiencia_profissional,
                    o.ilha,
                    o.concelho,
                    orientador.nome AS orientador_denominacao,
                    coordenador.nome AS coordenador_denominacao,
                    o.email_contacto,
                    o.contacto,
                    o.observacao,
                    o.estado,
                    EXISTS (
                        SELECT 1
                        FROM emprego_t_candidatura_oferta candidatura
                        WHERE candidatura.id_oferta = o.id
                          AND candidatura.pessoa_id = ?
                    ) AS ja_candidatado
                FROM emprego_t_oferta o
                LEFT JOIN emprego_t_entidade_colaborador orientador ON orientador.id = o.orientador_id
                LEFT JOIN emprego_t_entidade_colaborador coordenador ON coordenador.id = o.coordenador_id
                WHERE o.id = ?
                  AND (""" + ESTADO_OFERTA_NORMALIZADO + ") IN ('ATIVA', 'FECHADA')";

        return empregoJdbcTemplate.query(sql, this::mapDetalhe, pessoaId, ofertaId)
                .stream()
                .findFirst();
    }

    public List<ConsultaVagaOpcaoResponse> listarEntidades() {
        return empregoJdbcTemplate.query(
                """
                        SELECT
                            CAST(o.entidade_id AS BIGINT) AS id,
                            MAX(o.denominacao_entidade) AS denominacao
                        FROM emprego_t_oferta o
                        WHERE o.entidade_id IS NOT NULL
                          AND NULLIF(TRIM(COALESCE(o.denominacao_entidade, '')), '') IS NOT NULL
                        GROUP BY o.entidade_id
                        ORDER BY MAX(o.denominacao_entidade)
                        """,
                (rs, rowNum) -> new ConsultaVagaOpcaoResponse(
                        getLong(rs, "id"),
                        null,
                        rs.getString("denominacao")
                )
        );
    }

    public List<ConsultaVagaOpcaoResponse> listarIlhas() {
        return empregoJdbcTemplate.query(
                """
                        SELECT DISTINCT
                            CASE
                                WHEN TRIM(o.ilha) ~ '^[0-9]+$' THEN CAST(TRIM(o.ilha) AS BIGINT)
                                ELSE NULL
                            END AS id,
                            TRIM(o.ilha) AS codigo,
                            TRIM(o.ilha) AS nome
                        FROM emprego_t_oferta o
                        WHERE NULLIF(TRIM(COALESCE(o.ilha, '')), '') IS NOT NULL
                        ORDER BY TRIM(o.ilha)
                        """,
                this::mapOpcaoGeografia
        );
    }

    public List<ConsultaVagaOpcaoResponse> listarConcelhos(Long ilhaId) {
        if (ilhaId == null) {
            return List.of();
        }
        return empregoJdbcTemplate.query(
                """
                        SELECT DISTINCT
                            CASE
                                WHEN TRIM(o.concelho) ~ '^[0-9]+$' THEN CAST(TRIM(o.concelho) AS BIGINT)
                                ELSE NULL
                            END AS id,
                            TRIM(o.concelho) AS codigo,
                            TRIM(o.concelho) AS nome
                        FROM emprego_t_oferta o
                        WHERE TRIM(COALESCE(o.ilha, '')) = ?
                          AND NULLIF(TRIM(COALESCE(o.concelho, '')), '') IS NOT NULL
                        ORDER BY TRIM(o.concelho)
                        """,
                this::mapOpcaoGeografia,
                String.valueOf(ilhaId)
        );
    }

    public Optional<String> buscarNomePessoa(Long pessoaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT nome
                        FROM emprego_t_utente
                        WHERE CAST(pessoa_id AS BIGINT) = ?
                        ORDER BY date_create DESC NULLS LAST, id DESC
                        FETCH FIRST 1 ROWS ONLY
                        """,
                (rs, rowNum) -> rs.getString("nome"),
                pessoaId
        ).stream().filter(this::temTexto).findFirst();
    }

    public Optional<CandidaturaAnterior> buscarCandidaturaDaOferta(Integer ofertaId, Long pessoaId) {
        return buscarCandidatura(
                "WHERE id_oferta = ? AND pessoa_id = ? ORDER BY date_create DESC NULLS LAST, id DESC",
                ofertaId,
                pessoaId
        );
    }

    public Optional<CandidaturaAnterior> buscarUltimaCandidatura(Long pessoaId) {
        return buscarCandidatura(
                "WHERE pessoa_id = ? ORDER BY date_create DESC NULLS LAST, id DESC",
                pessoaId
        );
    }

    public Integer inserirCandidatura(
            Long pessoaId,
            String nome,
            OfertaDetalhe oferta,
            String habilitacaoAcademica,
            String areaFormacao,
            String utilizador
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO emprego_t_candidatura_oferta (
                                pessoa_id, nome, tipo_oferta, id_oferta, entidade_id,
                                selecao_iefp, canal, status_candidatura, habilitacao_academica,
                                area_formacao, anexos, date_create, user_create
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, CURRENT_TIMESTAMP, ?)
                            """,
                    new String[]{"id"}
            );
            statement.setLong(1, pessoaId);
            statement.setString(2, nome);
            statement.setString(3, oferta.tipoOferta());
            statement.setInt(4, oferta.id());
            setInteger(statement, 5, oferta.entidadeId());
            statement.setBoolean(6, false);
            statement.setString(7, "PORTAL");
            statement.setString(8, "TRIAGEM");
            statement.setString(9, habilitacaoAcademica);
            statement.setString(10, areaFormacao);
            statement.setString(11, utilizador);
            return statement;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public void atualizarAnexos(Integer candidaturaId, Object anexos) {
        empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE emprego_t_candidatura_oferta SET anexos = ? WHERE id = ?"
            );
            statement.setObject(1, escreverJson(anexos), Types.OTHER);
            statement.setInt(2, candidaturaId);
            return statement;
        });
    }

    private Optional<CandidaturaAnterior> buscarCandidatura(String where, Object... parametros) {
        String sql = """
                SELECT id, habilitacao_academica, area_formacao, anexos, date_create
                FROM emprego_t_candidatura_oferta
                """ + where + " FETCH FIRST 1 ROWS ONLY";
        return empregoJdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CandidaturaAnterior(
                        rs.getInt("id"),
                        rs.getString("habilitacao_academica"),
                        rs.getString("area_formacao"),
                        lerJson(rs.getObject("anexos")),
                        rs.getObject("date_create", LocalDateTime.class)
                ),
                parametros
        ).stream().findFirst();
    }

    private String construirWhere(ConsultaVagaFiltro filtro, List<Object> parametros) {
        StringBuilder where = new StringBuilder(
                " WHERE (" + ESTADO_OFERTA_NORMALIZADO + ") IN ('ATIVA', 'FECHADA')"
        );
        if (temTexto(filtro.tipoOferta())) {
            where.append(" AND (").append(TIPO_OFERTA_NORMALIZADO).append(") = ?");
            parametros.add(filtro.tipoOferta());
        }
        if (filtro.entidadeId() != null) {
            where.append(" AND o.entidade_id = ?");
            parametros.add(filtro.entidadeId());
        }
        adicionarFiltroTexto(where, parametros, "o.ilha", filtro.ilha());
        adicionarFiltroTexto(where, parametros, "o.concelho", filtro.concelho());
        if (temTexto(filtro.estado())) {
            where.append(" AND (").append(ESTADO_OFERTA_NORMALIZADO).append(") = ?");
            parametros.add(filtro.estado());
        }
        adicionarFiltroTexto(where, parametros, "o.codigo_referencia", filtro.codigoReferencia());
        if (filtro.dataInicio() != null) {
            where.append(" AND o.data_inicio_candidatura >= ?");
            parametros.add(filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            where.append(" AND o.data_fim_candidatura <= ?");
            parametros.add(filtro.dataFim());
        }
        if (temTexto(filtro.pesquisa())) {
            where.append("""
                    AND (
                        o.titulo ILIKE ?
                        OR o.denominacao_entidade ILIKE ?
                        OR o.codigo_referencia ILIKE ?
                    )
                    """);
            String pesquisa = "%" + filtro.pesquisa().trim() + "%";
            parametros.add(pesquisa);
            parametros.add(pesquisa);
            parametros.add(pesquisa);
        }
        return where.toString();
    }

    private void adicionarFiltroTexto(
            StringBuilder where,
            List<Object> parametros,
            String coluna,
            String valor
    ) {
        if (!temTexto(valor)) {
            return;
        }
        where.append(" AND UPPER(TRIM(").append(coluna).append(")) = UPPER(TRIM(?))");
        parametros.add(valor.trim());
    }

    private OfertaResumo mapResumo(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new OfertaResumo(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("tipo_oferta"),
                rs.getString("ilha"),
                rs.getString("concelho"),
                rs.getObject("num_vagas", Integer.class),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("denominacao_entidade"),
                rs.getString("codigo_referencia"),
                rs.getString("estado"),
                rs.getObject("data_inicio_candidatura", LocalDate.class),
                rs.getObject("data_fim_candidatura", LocalDate.class),
                rs.getBoolean("ja_candidatado")
        );
    }

    private OfertaDetalhe mapDetalhe(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new OfertaDetalhe(
                rs.getInt("id"),
                rs.getString("codigo_referencia"),
                rs.getString("tipo_oferta"),
                rs.getString("titulo"),
                rs.getString("descricao"),
                rs.getObject("data_inicio_candidatura", LocalDate.class),
                rs.getObject("data_fim_candidatura", LocalDate.class),
                rs.getObject("data_inicio_previsto", LocalDate.class),
                rs.getObject("duracao_contrato", Integer.class),
                rs.getString("regime_contrato"),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("denominacao_entidade"),
                rs.getString("habilitacao_minima"),
                rs.getString("nivel_qualificacao"),
                rs.getObject("num_vagas", Integer.class),
                rs.getString("habilitacao_maxima"),
                lerJson(rs.getObject("conhecimento_linguistico")),
                lerJson(rs.getObject("competencias_valorizadas")),
                rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fim", LocalTime.class),
                lerJson(rs.getObject("dias_semana")),
                lerJson(rs.getObject("cursos_area_formacao")),
                lerJson(rs.getObject("experiencia_profissional")),
                rs.getString("ilha"),
                rs.getString("concelho"),
                rs.getString("orientador_denominacao"),
                rs.getString("coordenador_denominacao"),
                rs.getString("email_contacto"),
                rs.getString("contacto"),
                rs.getString("observacao"),
                rs.getString("estado"),
                rs.getBoolean("ja_candidatado")
        );
    }

    private ConsultaVagaOpcaoResponse mapOpcaoGeografia(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new ConsultaVagaOpcaoResponse(
                getLong(rs, "id"),
                rs.getString("codigo"),
                rs.getString("nome")
        );
    }

    private Long getLong(java.sql.ResultSet rs, String coluna) throws java.sql.SQLException {
        Number valor = (Number) rs.getObject(coluna);
        return valor == null ? null : valor.longValue();
    }

    private void setInteger(PreparedStatement statement, int indice, Integer valor) throws java.sql.SQLException {
        if (valor == null) {
            statement.setNull(indice, Types.INTEGER);
        } else {
            statement.setInt(indice, valor);
        }
    }

    private Object lerJson(Object valor) throws java.sql.SQLException {
        if (valor == null) {
            return null;
        }
        try {
            return objectMapper.readValue(valor.toString(), Object.class);
        } catch (Exception ex) {
            throw new java.sql.SQLException("Não foi possível interpretar os documentos guardados.", ex);
        }
    }

    private String escreverJson(Object valor) throws java.sql.SQLException {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (Exception ex) {
            throw new java.sql.SQLException("Não foi possível preparar os documentos da candidatura.", ex);
        }
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    public record OfertaResumo(
            Integer id,
            String titulo,
            String tipoOferta,
            String ilha,
            String concelho,
            Integer numVagas,
            Integer entidadeId,
            String denominacaoEntidade,
            String codigoReferencia,
            String estado,
            LocalDate dataInicioCandidatura,
            LocalDate dataFimCandidatura,
            Boolean jaCandidatado
    ) {
    }

    public record OfertaDetalhe(
            Integer id,
            String codigoReferencia,
            String tipoOferta,
            String titulo,
            String descricao,
            LocalDate dataInicioCandidatura,
            LocalDate dataFimCandidatura,
            LocalDate dataInicioPrevisto,
            Integer duracaoContrato,
            String regimeContrato,
            Integer entidadeId,
            String denominacaoEntidade,
            String habilitacaoMinima,
            String nivelQualificacao,
            Integer numVagas,
            String habilitacaoMaxima,
            Object conhecimentoLinguistico,
            Object competenciasValorizadas,
            LocalTime horaInicio,
            LocalTime horaFim,
            Object diasSemana,
            Object cursosAreaFormacao,
            Object experienciaProfissional,
            String ilha,
            String concelho,
            String orientadorDenominacao,
            String coordenadorDenominacao,
            String emailContacto,
            String contacto,
            String observacao,
            String estado,
            Boolean jaCandidatado
    ) {
    }

    public record CandidaturaAnterior(
            Integer id,
            String habilitacaoAcademica,
            String areaFormacao,
            Object anexos,
            LocalDateTime dataCandidatura
    ) {
    }
}
