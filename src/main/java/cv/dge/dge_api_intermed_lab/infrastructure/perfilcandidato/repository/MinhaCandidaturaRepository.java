package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaFiltro;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MinhaCandidaturaRepository {

    private static final String TIPO_OFERTA_NORMALIZADO = """
            CASE UPPER(TRIM(COALESCE(c.tipo_oferta, o.tipo_oferta, '')))
                WHEN 'EMPREGO' THEN 'OFERTA_EMPREGO'
                WHEN 'ESTAGIO' THEN 'OFERTA_ESTAGIO'
                WHEN 'ESTÁGIO' THEN 'OFERTA_ESTAGIO'
                WHEN 'ESTAGIO_PROFISSIONAL' THEN 'OFERTA_ESTAGIO'
                ELSE UPPER(TRIM(COALESCE(c.tipo_oferta, o.tipo_oferta, '')))
            END
            """;

    private static final String STATUS_CANDIDATURA_NORMALIZADO = """
            CASE UPPER(TRIM(COALESCE(c.status_candidatura, '')))
                WHEN 'APROVAR' THEN 'APROVADO'
                WHEN 'APROVADA' THEN 'APROVADO'
                WHEN 'RECUSAR' THEN 'RECUSADO'
                WHEN 'RECUSADA' THEN 'RECUSADO'
                ELSE UPPER(TRIM(COALESCE(c.status_candidatura, '')))
            END
            """;

    private static final String CAMPOS_CANDIDATURA = """
            c.id,
            COALESCE(c.tipo_oferta, o.tipo_oferta) AS tipo_oferta,
            c.id_oferta,
            o.titulo,
            o.codigo_referencia,
            COALESCE(c.entidade_id, o.entidade_id) AS entidade_id,
            o.denominacao_entidade,
            o.ilha,
            o.concelho,
            c.status_candidatura,
            c.motivo_recusa,
            c.canal,
            c.anexos,
            c.date_create
            """;

    private final JdbcTemplate empregoJdbcTemplate;
    private final ObjectMapper objectMapper;

    public MinhaCandidaturaRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            ObjectMapper objectMapper
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.objectMapper = objectMapper;
    }

    public List<CandidaturaRegisto> listar(MinhaCandidaturaFiltro filtro) {
        List<Object> parametros = new ArrayList<>();
        String where = construirWhere(filtro, parametros);
        String sql = """
                SELECT
                """ + CAMPOS_CANDIDATURA + """
                FROM emprego_t_candidatura_oferta c
                LEFT JOIN emprego_t_oferta o ON o.id = c.id_oferta
                """ + where + """
                ORDER BY c.date_create DESC NULLS LAST, c.id DESC
                """;

        return empregoJdbcTemplate.query(sql, this::mapCandidatura, parametros.toArray());
    }

    public Optional<CandidaturaRegisto> buscarPorId(Integer candidaturaId, Long pessoaId) {
        String sql = """
                SELECT
                """ + CAMPOS_CANDIDATURA + """
                FROM emprego_t_candidatura_oferta c
                LEFT JOIN emprego_t_oferta o ON o.id = c.id_oferta
                WHERE c.id = ?
                  AND c.pessoa_id = ?
                """;

        return empregoJdbcTemplate.query(sql, this::mapCandidatura, candidaturaId, pessoaId)
                .stream()
                .findFirst();
    }

    public List<ConsultaVagaOpcaoResponse> listarEntidades(Long pessoaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT
                            COALESCE(c.entidade_id, o.entidade_id) AS entidade_id,
                            MAX(NULLIF(TRIM(o.denominacao_entidade), '')) AS denominacao
                        FROM emprego_t_candidatura_oferta c
                        LEFT JOIN emprego_t_oferta o ON o.id = c.id_oferta
                        WHERE c.pessoa_id = ?
                          AND COALESCE(c.entidade_id, o.entidade_id) IS NOT NULL
                        GROUP BY COALESCE(c.entidade_id, o.entidade_id)
                        ORDER BY COALESCE(
                            MAX(NULLIF(TRIM(o.denominacao_entidade), '')),
                            CAST(COALESCE(c.entidade_id, o.entidade_id) AS VARCHAR)
                        )
                        """,
                (rs, rowNum) -> {
                    Long entidadeId = rs.getObject("entidade_id", Long.class);
                    String denominacao = rs.getString("denominacao");
                    return new ConsultaVagaOpcaoResponse(
                            entidadeId,
                            null,
                            temTexto(denominacao) ? denominacao : String.valueOf(entidadeId)
                    );
                },
                pessoaId
        );
    }

    public List<ConsultaVagaOpcaoResponse> listarIlhas(Long pessoaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT DISTINCT TRIM(o.ilha) AS codigo
                        FROM emprego_t_candidatura_oferta c
                        INNER JOIN emprego_t_oferta o ON o.id = c.id_oferta
                        WHERE c.pessoa_id = ?
                          AND NULLIF(TRIM(COALESCE(o.ilha, '')), '') IS NOT NULL
                        ORDER BY TRIM(o.ilha)
                        """,
                (rs, rowNum) -> mapOpcaoGeografia(rs.getString("codigo")),
                pessoaId
        );
    }

    public List<ConsultaVagaOpcaoResponse> listarConcelhos(Long pessoaId, String ilha) {
        if (!temTexto(ilha)) {
            return List.of();
        }
        return empregoJdbcTemplate.query(
                """
                        SELECT DISTINCT TRIM(o.concelho) AS codigo
                        FROM emprego_t_candidatura_oferta c
                        INNER JOIN emprego_t_oferta o ON o.id = c.id_oferta
                        WHERE c.pessoa_id = ?
                          AND UPPER(TRIM(COALESCE(o.ilha, ''))) = UPPER(?)
                          AND NULLIF(TRIM(COALESCE(o.concelho, '')), '') IS NOT NULL
                        ORDER BY TRIM(o.concelho)
                        """,
                (rs, rowNum) -> mapOpcaoGeografia(rs.getString("codigo")),
                pessoaId,
                ilha.trim()
        );
    }

    private String construirWhere(MinhaCandidaturaFiltro filtro, List<Object> parametros) {
        StringBuilder where = new StringBuilder("WHERE c.pessoa_id = ?\n");
        parametros.add(filtro.pessoaId());

        if (temTexto(filtro.tipoOferta())) {
            where.append("AND (").append(TIPO_OFERTA_NORMALIZADO).append(") = ?\n");
            parametros.add(filtro.tipoOferta());
        }
        if (filtro.entidadeId() != null) {
            where.append("AND COALESCE(c.entidade_id, o.entidade_id) = ?\n");
            parametros.add(filtro.entidadeId());
        }
        if (temTexto(filtro.ilha())) {
            where.append("AND UPPER(TRIM(COALESCE(o.ilha, ''))) = UPPER(?)\n");
            parametros.add(filtro.ilha());
        }
        if (temTexto(filtro.concelho())) {
            where.append("AND UPPER(TRIM(COALESCE(o.concelho, ''))) = UPPER(?)\n");
            parametros.add(filtro.concelho());
        }
        if (temTexto(filtro.estado())) {
            where.append("AND (").append(STATUS_CANDIDATURA_NORMALIZADO).append(") = ?\n");
            parametros.add(filtro.estado());
        }
        if (temTexto(filtro.codigoReferencia())) {
            where.append("AND POSITION(UPPER(?) IN UPPER(COALESCE(o.codigo_referencia, ''))) > 0\n");
            parametros.add(filtro.codigoReferencia());
        }
        if (filtro.dataInicio() != null) {
            where.append("AND c.date_create >= ?\n");
            parametros.add(filtro.dataInicio().atStartOfDay());
        }
        if (filtro.dataFim() != null) {
            where.append("AND c.date_create <= ?\n");
            parametros.add(LocalDateTime.of(filtro.dataFim(), LocalTime.MAX));
        }
        return where.toString();
    }

    private CandidaturaRegisto mapCandidatura(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new CandidaturaRegisto(
                rs.getInt("id"),
                rs.getString("tipo_oferta"),
                rs.getObject("id_oferta", Integer.class),
                rs.getString("titulo"),
                rs.getString("codigo_referencia"),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("denominacao_entidade"),
                rs.getString("ilha"),
                rs.getString("concelho"),
                rs.getString("status_candidatura"),
                rs.getString("motivo_recusa"),
                rs.getString("canal"),
                lerJson(rs.getString("anexos")),
                rs.getObject("date_create", LocalDateTime.class)
        );
    }

    private JsonNode lerJson(String json) {
        if (!temTexto(json)) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new DataRetrievalFailureException("Conteúdo inválido no campo de anexos da candidatura.", ex);
        }
    }

    private ConsultaVagaOpcaoResponse mapOpcaoGeografia(String codigo) {
        return new ConsultaVagaOpcaoResponse(converterLong(codigo), codigo, codigo);
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

    public record CandidaturaRegisto(
            Integer candidaturaId,
            String tipoOferta,
            Integer ofertaId,
            String titulo,
            String codigoReferencia,
            Integer entidadeId,
            String entidade,
            String ilha,
            String concelho,
            String estado,
            String motivoRecusa,
            String canal,
            JsonNode anexos,
            LocalDateTime dataCandidatura
    ) {
    }
}
