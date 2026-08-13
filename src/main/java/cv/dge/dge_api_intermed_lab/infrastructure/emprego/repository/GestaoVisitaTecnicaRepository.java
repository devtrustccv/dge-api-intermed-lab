package cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaAtualizacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaCefpSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VisitaTecnicaRequest;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
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
public class GestaoVisitaTecnicaRepository {

    private static final String SELECT_BASE = """
            SELECT
                v.id,
                v.entidade_id,
                v.data_visita,
                v.visitante,
                v.hora_inicio,
                v.hora_fim,
                v.objetivos,
                v.agendado_por,
                v.cefp_id,
                COALESCE(NULLIF(TRIM(v.cefp), ''), c.denominacao) AS cefp,
                v.estado,
                v.candidatos,
                v.nova_data,
                v.motivo_indeferimento,
                v.observacoes_entidade,
                v.supervisor_participante,
                v.observacoes_iefp,
                v.detalhes_avaliacao,
                v.conteudo_reuniao,
                v.date_create,
                v.user_create,
                v.date_update,
                v.user_update
            FROM emprego_t_visitas v
            LEFT JOIN emprego_t_cefp c ON c.id = v.cefp_id
            """;

    private static final String SQL_INSERT = """
            INSERT INTO emprego_t_visitas (
                entidade_id,
                visitante,
                data_visita,
                hora_inicio,
                hora_fim,
                objetivos,
                candidatos,
                estado,
                agendado_por,
                cefp_id,
                cefp,
                date_create,
                user_create
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_UPDATE = """
            UPDATE emprego_t_visitas
            SET data_visita = ?,
                visitante = ?,
                hora_inicio = ?,
                hora_fim = ?,
                objetivos = ?,
                candidatos = ?,
                observacoes_entidade = ?,
                supervisor_participante = ?,
                observacoes_iefp = ?,
                detalhes_avaliacao = ?,
                date_update = ?,
                user_update = ?
            WHERE id = ?
            """;

    private final JdbcTemplate empregoJdbcTemplate;
    private final JdbcTemplate globalJdbcTemplate;
    private final ObjectMapper objectMapper;

    public GestaoVisitaTecnicaRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("tertiaryDataSource") DataSource tertiaryDataSource,
            ObjectMapper objectMapper
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.globalJdbcTemplate = new JdbcTemplate(tertiaryDataSource);
        this.objectMapper = objectMapper;
    }

    public List<VisitaTecnicaListaResponse> listar(VisitaTecnicaFiltro filtro) {
        List<Object> params = new ArrayList<>();
        String sql = SELECT_BASE + construirWhere(filtro, params) + """
                ORDER BY v.data_visita DESC NULLS LAST, v.date_create DESC NULLS LAST, v.id DESC
                """;
        return empregoJdbcTemplate.query(sql, this::mapLista, params.toArray());
    }

    public Optional<VisitaTecnicaDetalheResponse> buscarPorId(Integer id) {
        List<VisitaTecnicaDetalheResponse> resultados = empregoJdbcTemplate.query(
                SELECT_BASE + " WHERE v.id = ?",
                this::mapDetalhe,
                id
        );
        return resultados.stream().findFirst();
    }

    public Integer inserir(
            VisitaTecnicaRequest request,
            String estado,
            String agendadoPor,
            String utilizador
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime agora = LocalDateTime.now();

        empregoJdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_INSERT, new String[]{"id"});
            int index = 1;
            setInteger(ps, index++, request.entidadeId());
            ps.setString(index++, request.visitante());
            ps.setObject(index++, request.dataVisita());
            ps.setObject(index++, request.horaInicio());
            ps.setObject(index++, request.horaFim());
            ps.setString(index++, request.objetivos());
            setJsonb(ps, index++, request.candidatos());
            ps.setString(index++, estado);
            ps.setString(index++, agendadoPor);
            setInteger(ps, index++, request.cefpId());
            ps.setString(index++, request.cefp());
            ps.setTimestamp(index++, Timestamp.valueOf(agora));
            ps.setString(index, utilizador);
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public void atualizar(Integer id, VisitaTecnicaAtualizacaoRequest request, String utilizador) {
        empregoJdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE);
            int index = 1;
            ps.setObject(index++, request.dataVisita());
            ps.setString(index++, request.visitante());
            ps.setObject(index++, request.horaInicio());
            ps.setObject(index++, request.horaFim());
            ps.setString(index++, request.objetivos());
            setJsonb(ps, index++, request.candidatos());
            ps.setString(index++, request.observacoesEntidade());
            ps.setString(index++, request.supervisorParticipante());
            ps.setString(index++, request.observacoesIefp());
            setJsonb(ps, index++, request.detalhesAvaliacao());
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(index++, utilizador);
            ps.setInt(index, id);
            return ps;
        });
    }

    public void validar(
            Integer id,
            String estado,
            java.time.LocalDate novaData,
            String motivoIndeferimento,
            String utilizador
    ) {
        empregoJdbcTemplate.update(
                """
                UPDATE emprego_t_visitas
                SET estado = ?,
                    nova_data = ?,
                    motivo_indeferimento = ?,
                    date_update = ?,
                    user_update = ?
                WHERE id = ?
                """,
                estado,
                novaData,
                motivoIndeferimento,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    public void alterarEstado(Integer id, String estado, String utilizador) {
        empregoJdbcTemplate.update(
                """
                UPDATE emprego_t_visitas
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

    public void registarObservacoes(
            Integer id,
            String observacoesEntidade,
            String supervisorParticipante,
            String utilizador
    ) {
        empregoJdbcTemplate.update(
                """
                UPDATE emprego_t_visitas
                SET observacoes_entidade = ?,
                    supervisor_participante = ?,
                    date_update = ?,
                    user_update = ?
                WHERE id = ?
                """,
                observacoesEntidade,
                supervisorParticipante,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    public List<VisitaTecnicaCandidatoSelectResponse> listarCandidatos(Integer entidadeId) {
        return empregoJdbcTemplate.query(
                """
                SELECT
                    pessoa_id,
                    MAX(NULLIF(TRIM(nome), '')) AS nome
                FROM emprego_t_candidatura_oferta
                WHERE entidade_id = ?
                  AND pessoa_id IS NOT NULL
                GROUP BY pessoa_id
                ORDER BY nome ASC NULLS LAST, pessoa_id ASC
                """,
                (rs, rowNum) -> {
                    Long pessoaId = getLong(rs, "pessoa_id");
                    String nome = textoOuPadrao(rs.getString("nome"), buscarNomePessoa(pessoaId).orElse(null));
                    return new VisitaTecnicaCandidatoSelectResponse(pessoaId, nome);
                },
                entidadeId
        );
    }

    public List<VisitaTecnicaCefpSelectResponse> listarCefps() {
        return empregoJdbcTemplate.query(
                """
                SELECT id, denominacao
                FROM emprego_t_cefp
                WHERE id IS NOT NULL
                  AND NULLIF(TRIM(denominacao), '') IS NOT NULL
                ORDER BY denominacao ASC NULLS LAST, id ASC
                """,
                (rs, rowNum) -> new VisitaTecnicaCefpSelectResponse(
                        getInteger(rs, "id"),
                        rs.getString("denominacao")
                )
        );
    }

    public Optional<String> buscarCefpDenominacao(Integer cefpId) {
        if (cefpId == null) {
            return Optional.empty();
        }
        List<String> resultados = empregoJdbcTemplate.query(
                """
                SELECT denominacao
                FROM emprego_t_cefp
                WHERE id = ?
                """,
                (rs, rowNum) -> rs.getString("denominacao"),
                cefpId
        );
        return resultados.stream()
                .filter(this::temTexto)
                .findFirst();
    }

    private String construirWhere(VisitaTecnicaFiltro filtro, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (filtro.entidadeId() != null) {
            where.append(" AND v.entidade_id = ?");
            params.add(filtro.entidadeId());
        }
        adicionarFiltroTexto(where, params, "v.estado", filtro.estado());
        adicionarFiltroTexto(where, params, "v.agendado_por", filtro.agendadoPor());
        if (filtro.cefpId() != null) {
            where.append(" AND v.cefp_id = ?");
            params.add(filtro.cefpId());
        }
        if (filtro.dataVisita() != null) {
            where.append(" AND v.data_visita = ?");
            params.add(filtro.dataVisita());
        }
        if (filtro.dataInicio() != null) {
            where.append(" AND v.date_create::date >= ?");
            params.add(filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            where.append(" AND v.date_create::date <= ?");
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

    private VisitaTecnicaListaResponse mapLista(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new VisitaTecnicaListaResponse(
                getInteger(rs, "id"),
                getInteger(rs, "entidade_id"),
                rs.getObject("data_visita", java.time.LocalDate.class),
                rs.getObject("hora_inicio", java.time.LocalTime.class),
                rs.getObject("hora_fim", java.time.LocalTime.class),
                null,
                rs.getString("visitante"),
                rs.getString("agendado_por"),
                rs.getString("agendado_por"),
                getInteger(rs, "cefp_id"),
                rs.getString("cefp"),
                rs.getString("estado"),
                rs.getString("estado"),
                null,
                null,
                null
        );
    }

    private VisitaTecnicaDetalheResponse mapDetalhe(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {
        return new VisitaTecnicaDetalheResponse(
                getInteger(rs, "id"),
                getInteger(rs, "entidade_id"),
                rs.getObject("data_visita", java.time.LocalDate.class),
                rs.getString("visitante"),
                rs.getObject("hora_inicio", java.time.LocalTime.class),
                rs.getObject("hora_fim", java.time.LocalTime.class),
                null,
                rs.getString("objetivos"),
                rs.getString("agendado_por"),
                rs.getString("agendado_por"),
                getInteger(rs, "cefp_id"),
                rs.getString("cefp"),
                rs.getString("estado"),
                rs.getString("estado"),
                readJson(rs.getObject("candidatos")),
                rs.getObject("nova_data", java.time.LocalDate.class),
                rs.getString("motivo_indeferimento"),
                rs.getString("observacoes_entidade"),
                rs.getString("supervisor_participante"),
                rs.getString("observacoes_iefp"),
                readJson(rs.getObject("detalhes_avaliacao")),
                rs.getString("conteudo_reuniao"),
                null,
                null,
                null,
                rs.getObject("date_create", java.time.LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", java.time.LocalDateTime.class),
                rs.getString("user_update")
        );
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

    private void setJsonb(PreparedStatement ps, int index, Object value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, Types.OTHER);
            return;
        }
        try {
            ps.setObject(index, objectMapper.writeValueAsString(value), Types.OTHER);
        } catch (Exception ex) {
            throw new java.sql.SQLException("JSON invalido para campo jsonb.", ex);
        }
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

    private void setInteger(PreparedStatement ps, int index, Integer value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
            return;
        }
        ps.setInt(index, value);
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

    private String textoOuPadrao(String valor, String padrao) {
        return temTexto(valor) ? valor.trim() : texto(padrao);
    }

    private String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
