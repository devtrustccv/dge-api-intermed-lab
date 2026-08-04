package cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaResponse;
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
public class GestaoVagaRepository {

    private static final String CAMPOS_DETALHE = """
            id,
            codigo_referencia,
            tipo_oferta,
            titulo,
            descricao,
            data_inicio_candidatura,
            data_fim_candidatura,
            data_inicio_previsto,
            duracao_contrato,
            regime_contrato,
            entidade_id,
            denominacao_entidade,
            habilitacao_minima,
            nivel_qualificacao,
            num_vagas,
            habilitacao_maxima,
            conhecimento_linguistico,
            competencias_valorizadas,
            hora_inicio,
            hora_fim,
            dias_semana,
            cursos_area_formacao,
            experiencia_profissional,
            ilha,
            concelho,
            orientador_id,
            coordenador_id,
            email_contacto,
            contacto,
            observacao,
            estado,
            date_create,
            user_create,
            date_update,
            user_update
            """;

    private static final String SQL_INSERT = """
            INSERT INTO emprego_t_oferta (
                codigo_referencia,
                tipo_oferta,
                titulo,
                descricao,
                data_inicio_candidatura,
                data_fim_candidatura,
                data_inicio_previsto,
                duracao_contrato,
                regime_contrato,
                entidade_id,
                denominacao_entidade,
                habilitacao_minima,
                nivel_qualificacao,
                num_vagas,
                habilitacao_maxima,
                conhecimento_linguistico,
                competencias_valorizadas,
                hora_inicio,
                hora_fim,
                dias_semana,
                cursos_area_formacao,
                experiencia_profissional,
                ilha,
                concelho,
                orientador_id,
                coordenador_id,
                email_contacto,
                contacto,
                observacao,
                estado,
                date_create,
                user_create
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_UPDATE = """
            UPDATE emprego_t_oferta
            SET codigo_referencia = ?,
                tipo_oferta = ?,
                titulo = ?,
                descricao = ?,
                data_inicio_candidatura = ?,
                data_fim_candidatura = ?,
                data_inicio_previsto = ?,
                duracao_contrato = ?,
                regime_contrato = ?,
                entidade_id = ?,
                denominacao_entidade = ?,
                habilitacao_minima = ?,
                nivel_qualificacao = ?,
                num_vagas = ?,
                habilitacao_maxima = ?,
                conhecimento_linguistico = ?,
                competencias_valorizadas = ?,
                hora_inicio = ?,
                hora_fim = ?,
                dias_semana = ?,
                cursos_area_formacao = ?,
                experiencia_profissional = ?,
                ilha = ?,
                concelho = ?,
                orientador_id = ?,
                coordenador_id = ?,
                email_contacto = ?,
                contacto = ?,
                observacao = ?,
                date_update = ?,
                user_update = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public GestaoVagaRepository(
            @Qualifier("primaryDataSource") DataSource dataSource,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = objectMapper;
    }

    public List<VagaListaResponse> listar(VagaFiltro filtro) {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(filtro, params);
        String sql = """
                SELECT
                    id,
                    titulo,
                    tipo_oferta,
                    ilha,
                    concelho,
                    num_vagas,
                    entidade_id,
                    denominacao_entidade,
                    codigo_referencia,
                    estado,
                    data_fim_candidatura
                FROM emprego_t_oferta o
                """ + where + """
                ORDER BY o.date_create DESC NULLS LAST, o.id DESC
                """;

        return jdbcTemplate.query(sql, this::mapLista, params.toArray());
    }

    public Long contar(VagaFiltro filtro) {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(filtro, params);
        String sql = "SELECT COUNT(*) FROM emprego_t_oferta o " + where;
        Long total = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
        return total == null ? 0L : total;
    }

    public Optional<VagaResponse> buscarPorId(Integer id) {
        String sql = "SELECT " + CAMPOS_DETALHE + " FROM emprego_t_oferta WHERE id = ?";
        List<VagaResponse> resultados = jdbcTemplate.query(sql, this::mapDetalhe, id);
        return resultados.stream().findFirst();
    }

    public Integer inserir(VagaRequest request, String estado, String utilizador) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime agora = LocalDateTime.now();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_INSERT, new String[]{"id"});
            preencherCamposGravacao(ps, request, 1);
            ps.setString(30, estado);
            ps.setTimestamp(31, Timestamp.valueOf(agora));
            ps.setString(32, utilizador);
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public void atualizar(Integer id, VagaRequest request, String utilizador) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE);
            int index = preencherCamposGravacao(ps, request, 1);
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(index++, utilizador);
            ps.setInt(index, id);
            return ps;
        });
    }

    public void alterarEstado(Integer id, String estado, String observacao, String utilizador) {
        jdbcTemplate.update(
                """
                        UPDATE emprego_t_oferta
                        SET estado = ?,
                            observacao = ?,
                            date_update = ?,
                            user_update = ?
                        WHERE id = ?
                        """,
                estado,
                observacao,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    private String construirWhere(VagaFiltro filtro, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");

        adicionarFiltroTexto(where, params, "o.tipo_oferta", filtro.tipoOferta());
        adicionarFiltroNumero(where, params, "o.entidade_id", filtro.entidadeId());
        adicionarFiltroTexto(where, params, "o.ilha", filtro.ilha());
        adicionarFiltroTexto(where, params, "o.concelho", filtro.concelho());
        adicionarFiltroTexto(where, params, "o.estado", filtro.estado());
        adicionarFiltroTexto(where, params, "o.codigo_referencia", filtro.codigoReferencia());
        adicionarFiltroNumero(where, params, "o.orientador_id", filtro.orientadorId());
        adicionarFiltroNumero(where, params, "o.coordenador_id", filtro.coordenadorId());

        if (filtro.dataInicio() != null) {
            where.append(" AND o.data_inicio_candidatura >= ?");
            params.add(filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            where.append(" AND o.data_fim_candidatura <= ?");
            params.add(filtro.dataFim());
        }
        if (temTexto(filtro.pesquisa())) {
            where.append("""
                    AND (
                        o.titulo ILIKE ?
                        OR o.codigo_referencia ILIKE ?
                        OR o.denominacao_entidade ILIKE ?
                        OR o.tipo_oferta ILIKE ?
                        OR o.estado ILIKE ?
                        OR o.ilha ILIKE ?
                        OR o.concelho ILIKE ?
                    )
                    """);
            String pesquisa = "%" + filtro.pesquisa().trim() + "%";
            for (int i = 0; i < 7; i++) {
                params.add(pesquisa);
            }
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

    private void adicionarFiltroNumero(StringBuilder where, List<Object> params, String coluna, Integer valor) {
        if (valor == null) {
            return;
        }
        where.append(" AND ").append(coluna).append(" = ?");
        params.add(valor);
    }

    private int preencherCamposGravacao(PreparedStatement ps, VagaRequest request, int index) throws java.sql.SQLException {
        ps.setString(index++, request.codigoReferencia());
        ps.setString(index++, request.tipoOferta());
        ps.setString(index++, request.titulo());
        ps.setString(index++, request.descricao());
        ps.setObject(index++, request.dataInicioCandidatura());
        ps.setObject(index++, request.dataFimCandidatura());
        ps.setObject(index++, request.dataInicioPrevisto());
        setInteger(ps, index++, request.duracaoContrato());
        ps.setString(index++, request.regimeContrato());
        setInteger(ps, index++, request.entidadeId());
        ps.setString(index++, request.denominacaoEntidade());
        ps.setString(index++, request.habilitacaoMinima());
        ps.setString(index++, request.nivelQualificacao());
        setInteger(ps, index++, request.numVagas());
        ps.setString(index++, request.habilitacaoMaxima());
        setJsonb(ps, index++, request.conhecimentoLinguistico());
        setJsonb(ps, index++, request.competenciasValorizadas());
        ps.setObject(index++, request.horaInicio());
        ps.setObject(index++, request.horaFim());
        setJsonb(ps, index++, request.diasSemana());
        setJsonb(ps, index++, request.cursosAreaFormacao());
        setJsonb(ps, index++, request.experienciaProfissional());
        ps.setString(index++, request.ilha());
        ps.setString(index++, request.concelho());
        setInteger(ps, index++, request.orientadorId());
        setInteger(ps, index++, request.coordenadorId());
        ps.setString(index++, request.emailContacto());
        ps.setString(index++, request.contacto());
        ps.setString(index++, request.observacao());
        return index;
    }

    private void setInteger(PreparedStatement ps, int index, Integer value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
            return;
        }
        ps.setInt(index, value);
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

    private VagaListaResponse mapLista(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new VagaListaResponse(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("tipo_oferta"),
                rs.getString("tipo_oferta"),
                rs.getString("ilha"),
                rs.getString("ilha"),
                rs.getString("concelho"),
                rs.getString("concelho"),
                localOferta(rs.getString("ilha"), rs.getString("concelho")),
                rs.getObject("num_vagas", Integer.class),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("denominacao_entidade"),
                rs.getString("codigo_referencia"),
                rs.getString("estado"),
                rs.getString("estado"),
                rs.getObject("data_fim_candidatura", java.time.LocalDate.class)
        );
    }

    private VagaResponse mapDetalhe(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        String estado = rs.getString("estado");
        String ilha = rs.getString("ilha");
        String concelho = rs.getString("concelho");
        return new VagaResponse(
                rs.getInt("id"),
                rs.getString("codigo_referencia"),
                rs.getString("tipo_oferta"),
                rs.getString("tipo_oferta"),
                rs.getString("titulo"),
                rs.getString("descricao"),
                rs.getObject("data_inicio_candidatura", java.time.LocalDate.class),
                rs.getObject("data_fim_candidatura", java.time.LocalDate.class),
                rs.getObject("data_inicio_previsto", java.time.LocalDate.class),
                rs.getObject("duracao_contrato", Integer.class),
                rs.getString("regime_contrato"),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("denominacao_entidade"),
                rs.getString("habilitacao_minima"),
                rs.getString("nivel_qualificacao"),
                rs.getObject("num_vagas", Integer.class),
                rs.getString("habilitacao_maxima"),
                readJson(rs.getObject("conhecimento_linguistico")),
                readJson(rs.getObject("competencias_valorizadas")),
                rs.getObject("hora_inicio", java.time.LocalTime.class),
                rs.getObject("hora_fim", java.time.LocalTime.class),
                readJson(rs.getObject("dias_semana")),
                readJson(rs.getObject("cursos_area_formacao")),
                readJson(rs.getObject("experiencia_profissional")),
                ilha,
                ilha,
                concelho,
                concelho,
                localOferta(ilha, concelho),
                rs.getObject("orientador_id", Integer.class),
                rs.getObject("coordenador_id", Integer.class),
                rs.getString("email_contacto"),
                rs.getString("contacto"),
                rs.getString("observacao"),
                estado,
                estado,
                !"FECHADA".equalsIgnoreCase(String.valueOf(estado)),
                rs.getObject("date_create", java.time.LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", java.time.LocalDateTime.class),
                rs.getString("user_update")
        );
    }

    private String localOferta(String ilha, String concelho) {
        if (!temTexto(ilha)) {
            return concelho;
        }
        if (!temTexto(concelho)) {
            return ilha;
        }
        return ilha + " - " + concelho;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
