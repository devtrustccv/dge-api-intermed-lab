package cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidatoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaAgendamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaResponse;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
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
public class GestaoCandidaturaRepository {

    private static final String CAMPOS_CANDIDATURA_DETALHE = """
            c.id,
            c.pessoa_id,
            c.nome,
            c.tipo_oferta,
            c.id_oferta,
            c.entidade_id,
            c.selecao_iefp,
            c.canal,
            c.status_candidatura,
            c.anexos,
            c.motivo_recusa,
            c.habilitacao_academica,
            c.date_create,
            c.user_create,
            c.date_update,
            c.user_update,
            o.codigo_referencia,
            o.titulo,
            o.denominacao_entidade
            """;

    private final JdbcTemplate empregoJdbcTemplate;
    private final JdbcTemplate globalJdbcTemplate;
    private final ObjectMapper objectMapper;

    public GestaoCandidaturaRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("tertiaryDataSource") DataSource tertiaryDataSource,
            ObjectMapper objectMapper
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.globalJdbcTemplate = new JdbcTemplate(tertiaryDataSource);
        this.objectMapper = objectMapper;
    }

    public List<CandidaturaListaResponse> listar(CandidaturaFiltro filtro) {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(filtro, params);
        String sql = """
                SELECT
                    c.id,
                    c.pessoa_id,
                    c.nome,
                    c.tipo_oferta,
                    c.id_oferta,
                    c.canal,
                    c.status_candidatura,
                    c.selecao_iefp,
                    c.date_create,
                    o.titulo,
                    entrevista.id AS entrevista_id,
                    CASE
                        WHEN entrevista.id IS NULL THEN FALSE
                        ELSE UPPER(TRIM(COALESCE(entrevista.estado, 'PENDENTE'))) = 'PENDENTE'
                    END AS pode_registar_resultado_entrevista
                FROM emprego_t_candidatura_oferta c
                LEFT JOIN emprego_t_oferta o ON o.id = c.id_oferta
                LEFT JOIN LATERAL (
                    SELECT e.id, e.estado
                    FROM emprego_t_entrevista_oferta e
                    WHERE e.id_candidatura = c.id
                    ORDER BY e.date_create DESC NULLS LAST, e.id DESC
                    FETCH FIRST 1 ROWS ONLY
                ) entrevista ON TRUE
                """ + where + """
                ORDER BY c.date_create DESC NULLS LAST, c.id DESC
                """;

        return empregoJdbcTemplate.query(sql, (rs, rowNum) -> new CandidaturaListaResponse(
                rs.getInt("id"),
                getLong(rs, "pessoa_id"),
                textoOuPadrao(rs.getString("nome"), buscarNomePessoa(getLong(rs, "pessoa_id")).orElse(null)),
                rs.getString("tipo_oferta"),
                rs.getString("tipo_oferta"),
                rs.getObject("id_oferta", Integer.class),
                rs.getString("titulo"),
                rs.getString("canal"),
                rs.getString("canal"),
                rs.getString("status_candidatura"),
                rs.getString("status_candidatura"),
                rs.getObject("selecao_iefp", Boolean.class),
                null,
                null,
                rs.getObject("entrevista_id", Integer.class),
                rs.getBoolean("pode_registar_resultado_entrevista"),
                rs.getObject("date_create", LocalDateTime.class)
        ), params.toArray());
    }

    public boolean existeEntrevista(Integer candidaturaId) {
        Long total = empregoJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM emprego_t_entrevista_oferta WHERE id_candidatura = ?",
                Long.class,
                candidaturaId
        );
        return total != null && total > 0;
    }

    public Optional<CandidaturaDetalheResponse> buscarPorId(Integer id) {
        String sql = """
                SELECT
                """ + CAMPOS_CANDIDATURA_DETALHE + """
                FROM emprego_t_candidatura_oferta c
                LEFT JOIN emprego_t_oferta o ON o.id = c.id_oferta
                WHERE c.id = ?
                """;

        List<CandidaturaDetalheResponse> resultados = empregoJdbcTemplate.query(sql, (rs, rowNum) -> {
            Long pessoaId = getLong(rs, "pessoa_id");
            String nomeFallback = rs.getString("nome");
            String habilitacaoAcademica = rs.getString("habilitacao_academica");
            return new CandidaturaDetalheResponse(
                    rs.getInt("id"),
                    rs.getString("tipo_oferta"),
                    rs.getString("tipo_oferta"),
                    rs.getObject("id_oferta", Integer.class),
                    rs.getString("codigo_referencia"),
                    rs.getString("titulo"),
                    rs.getObject("entidade_id", Integer.class),
                    rs.getString("denominacao_entidade"),
                    rs.getObject("date_create", LocalDateTime.class),
                    buscarCandidato(pessoaId, nomeFallback, habilitacaoAcademica),
                    readJson(rs.getObject("anexos")),
                    rs.getString("status_candidatura"),
                    rs.getString("status_candidatura"),
                    rs.getString("motivo_recusa"),
                    rs.getObject("selecao_iefp", Boolean.class),
                    null,
                    null,
                    rs.getObject("date_create", LocalDateTime.class),
                    rs.getString("user_create"),
                    rs.getObject("date_update", LocalDateTime.class),
                    rs.getString("user_update")
            );
        }, id);

        return resultados.stream().findFirst();
    }

    public void atualizarAvaliacao(Integer id, String status, String motivoRecusa, String utilizador) {
        empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_candidatura_oferta
                        SET status_candidatura = ?,
                            motivo_recusa = ?,
                            date_update = ?,
                            user_update = ?
                        WHERE id = ?
                        """,
                status,
                motivoRecusa,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    public Integer inserirEntrevista(
            Integer candidaturaId,
            CandidaturaDetalheResponse candidatura,
            EntrevistaAgendamentoRequest request,
            String estado
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime agora = LocalDateTime.now();
        CandidatoDetalheResponse candidato = candidatura.candidato();

        empregoJdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                            INSERT INTO emprego_t_entrevista_oferta (
                                nome,
                                pessoa_id,
                                id_candidatura,
                                data_entrevista,
                                horario,
                                canal,
                                local_entrevista,
                                estado,
                                date_create,
                                user_create
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    new String[]{"id"}
            );
            ps.setString(1, candidato == null ? null : candidato.nome());
            setLong(ps, 2, candidato == null ? null : candidato.pessoaId());
            ps.setInt(3, candidaturaId);
            ps.setObject(4, request.dataEntrevista());
            ps.setObject(5, request.horario());
            ps.setString(6, request.canal());
            ps.setString(7, request.localEntrevista());
            ps.setString(8, estado);
            ps.setTimestamp(9, Timestamp.valueOf(agora));
            ps.setString(10, request.utilizador());
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public List<EntrevistaResponse> listarEntrevistas(Integer candidaturaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT
                            id,
                            id_candidatura,
                            pessoa_id,
                            nome,
                            data_entrevista,
                            horario,
                            canal,
                            local_entrevista,
                            parecer_entrevista,
                            resultado_entrevista,
                            estado,
                            date_create,
                            user_create,
                            date_update,
                            user_update
                        FROM emprego_t_entrevista_oferta
                        WHERE id_candidatura = ?
                        ORDER BY date_create DESC NULLS LAST, id DESC
                        """,
                this::mapEntrevista,
                candidaturaId
        );
    }

    public Optional<EntrevistaResponse> buscarEntrevista(Integer candidaturaId, Integer entrevistaId) {
        List<EntrevistaResponse> resultados = empregoJdbcTemplate.query(
                """
                        SELECT
                            id,
                            id_candidatura,
                            pessoa_id,
                            nome,
                            data_entrevista,
                            horario,
                            canal,
                            local_entrevista,
                            parecer_entrevista,
                            resultado_entrevista,
                            estado,
                            date_create,
                            user_create,
                            date_update,
                            user_update
                        FROM emprego_t_entrevista_oferta
                        WHERE id_candidatura = ?
                          AND id = ?
                        """,
                this::mapEntrevista,
                candidaturaId,
                entrevistaId
        );
        return resultados.stream().findFirst();
    }

    public void atualizarResultadoEntrevista(
            Integer candidaturaId,
            Integer entrevistaId,
            String parecer,
            String observacao,
            String estado,
            String utilizador
    ) {
        empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_entrevista_oferta
                        SET parecer_entrevista = ?,
                            resultado_entrevista = ?,
                            estado = ?,
                            date_update = ?,
                            user_update = ?
                        WHERE id_candidatura = ?
                          AND id = ?
                        """,
                parecer,
                observacao,
                estado,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                candidaturaId,
                entrevistaId
        );
    }

    private CandidatoDetalheResponse buscarCandidato(Long pessoaId, String nomeFallback, String habilitacaoAcademica) {
        if (pessoaId == null) {
            return new CandidatoDetalheResponse(
                    null, nomeFallback, null, null, null, null, null, null,
                    null, null, null, null, null, null, habilitacaoAcademica
            );
        }

        List<CandidatoDetalheResponse> resultados = globalJdbcTemplate.query(
                """
                        SELECT
                            id,
                            nome,
                            data_nasc,
                            sexo,
                            email,
                            telefone,
                            ilha_id,
                            concelho_id,
                            localidade_id
                        FROM ci_t_pessoa
                        WHERE id = ?
                        """,
                (rs, rowNum) -> {
                    String ilhaId = texto(rs.getObject("ilha_id"));
                    String concelhoId = texto(rs.getObject("concelho_id"));
                    String localidadeId = texto(rs.getObject("localidade_id"));
                    String ilhaDesc = buscarNomeGeografia(ilhaId).orElse(ilhaId);
                    String concelhoDesc = buscarNomeGeografia(concelhoId).orElse(concelhoId);
                    String localidadeDesc = buscarNomeGeografia(localidadeId).orElse(localidadeId);
                    String nome = textoOuPadrao(rs.getString("nome"), nomeFallback);

                    return new CandidatoDetalheResponse(
                            getLong(rs, "id"),
                            nome,
                            getLocalDate(rs, "data_nasc"),
                            rs.getString("sexo"),
                            rs.getString("email"),
                            rs.getString("telefone"),
                            ilhaId,
                            ilhaDesc,
                            concelhoId,
                            concelhoDesc,
                            localidadeId,
                            localidadeDesc,
                            juntarLocalizacao(ilhaDesc, concelhoDesc),
                            localidadeDesc,
                            habilitacaoAcademica
                    );
                },
                pessoaId
        );

        return resultados.stream().findFirst().orElseGet(() -> new CandidatoDetalheResponse(
                pessoaId, nomeFallback, null, null, null, null, null, null,
                null, null, null, null, null, null, habilitacaoAcademica
        ));
    }

    private Optional<String> buscarNomePessoa(Long pessoaId) {
        if (pessoaId == null) {
            return Optional.empty();
        }
        List<String> resultados = globalJdbcTemplate.query(
                """
                        SELECT nome
                        FROM ci_t_pessoa
                        WHERE id = ?
                        """,
                (rs, rowNum) -> rs.getString("nome"),
                pessoaId
        );
        return resultados.stream()
                .filter(this::temTexto)
                .findFirst();
    }

    private Optional<String> buscarNomeGeografia(String idOuCodigo) {
        if (!temTexto(idOuCodigo)) {
            return Optional.empty();
        }
        List<String> resultados = globalJdbcTemplate.query(
                """
                        SELECT nome
                        FROM global_geografia
                        WHERE id = ?
                           OR UPPER(codigo) = UPPER(?)
                        FETCH FIRST 1 ROWS ONLY
                        """,
                (rs, rowNum) -> rs.getString("nome"),
                idOuCodigo.trim(),
                idOuCodigo.trim()
        );
        return resultados.stream()
                .filter(this::temTexto)
                .findFirst();
    }

    private EntrevistaResponse mapEntrevista(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new EntrevistaResponse(
                rs.getInt("id"),
                rs.getObject("id_candidatura", Integer.class),
                getLong(rs, "pessoa_id"),
                rs.getString("nome"),
                rs.getObject("data_entrevista", LocalDate.class),
                rs.getObject("horario", LocalTime.class),
                rs.getString("canal"),
                rs.getString("canal"),
                rs.getString("local_entrevista"),
                rs.getString("parecer_entrevista"),
                rs.getString("parecer_entrevista"),
                rs.getString("resultado_entrevista"),
                rs.getString("estado"),
                rs.getString("estado"),
                rs.getObject("date_create", LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", LocalDateTime.class),
                rs.getString("user_update")
        );
    }

    private String construirWhere(CandidaturaFiltro filtro, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");

        if (filtro.candidatoId() != null) {
            where.append(" AND c.pessoa_id = ?");
            params.add(filtro.candidatoId());
        }
        adicionarFiltroTexto(where, params, "c.status_candidatura", filtro.estado());
        adicionarFiltroTexto(where, params, "c.tipo_oferta", filtro.tipoOferta());
        if (filtro.ofertaId() != null) {
            where.append(" AND c.id_oferta = ?");
            params.add(filtro.ofertaId());
        }
        adicionarFiltroTexto(where, params, "c.canal", filtro.canal());
        if (filtro.dataInicio() != null) {
            where.append(" AND c.date_create::date >= ?");
            params.add(filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            where.append(" AND c.date_create::date <= ?");
            params.add(filtro.dataFim());
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

    private Object readJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value.toString(), new TypeReference<>() {
            });
        } catch (Exception ex) {
            return value.toString();
        }
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

    private LocalDate getLocalDate(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.toString().trim();
        return texto.isEmpty() ? null : texto;
    }

    private String textoOuPadrao(String valor, String padrao) {
        return temTexto(valor) ? valor : padrao;
    }

    private String juntarLocalizacao(String ilha, String concelho) {
        if (!temTexto(ilha)) {
            return concelho;
        }
        if (!temTexto(concelho)) {
            return ilha;
        }
        return ilha + " / " + concelho;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
