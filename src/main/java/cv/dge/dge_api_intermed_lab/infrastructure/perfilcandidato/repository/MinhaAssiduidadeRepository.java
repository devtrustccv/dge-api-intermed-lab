package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeRequest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class MinhaAssiduidadeRepository {

    private static final String CAMPOS_ASSIDUIDADE = """
            assiduidade.id,
            assiduidade.id_colocacao,
            assiduidade.entidade_id,
            assiduidade.denominacao_entidade,
            assiduidade.pessoa_id,
            assiduidade.nome,
            assiduidade.data,
            assiduidade.hora_entrada,
            assiduidade.hora_saida,
            assiduidade.tipo_assiduidade,
            assiduidade.justificacao,
            assiduidade.estado,
            assiduidade.observacao,
            assiduidade.comprovativo,
            assiduidade.date_create,
            assiduidade.user_create,
            assiduidade.date_update,
            assiduidade.user_update
            """;

    private final JdbcTemplate empregoJdbcTemplate;

    public MinhaAssiduidadeRepository(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
    }

    public List<AssiduidadeRegisto> listar(MinhaAssiduidadeFiltro filtro) {
        List<Object> parametros = new ArrayList<>();
        String where = construirWhere(filtro, parametros);
        String sql = """
                SELECT
                """ + CAMPOS_ASSIDUIDADE + """
                FROM emprego_t_assiduidade assiduidade
                """ + where + """
                ORDER BY assiduidade.data DESC NULLS LAST,
                         assiduidade.date_create DESC NULLS LAST,
                         assiduidade.id DESC
                """;
        return empregoJdbcTemplate.query(sql, this::mapearAssiduidade, parametros.toArray());
    }

    public Optional<AssiduidadeRegisto> buscarPorId(Integer assiduidadeId, Long pessoaId) {
        String sql = """
                SELECT
                """ + CAMPOS_ASSIDUIDADE + """
                FROM emprego_t_assiduidade assiduidade
                WHERE assiduidade.id = ?
                  AND assiduidade.pessoa_id = ?
                """;
        return empregoJdbcTemplate.query(sql, this::mapearAssiduidade, assiduidadeId, pessoaId)
                .stream()
                .findFirst();
    }

    public Optional<ColocacaoAtiva> buscarColocacaoAtiva(Long pessoaId) {
        String sql = """
                SELECT colocacao.id,
                       colocacao.entidade_id,
                       colocacao.denominacao_entidade,
                       colocacao.pessoa_id,
                       colocacao.nome
                FROM emprego_t_colocacao_candidato colocacao
                WHERE colocacao.pessoa_id = ?
                  AND UPPER(TRIM(COALESCE(colocacao.estado, ''))) IN ('A', 'ATIVO')
                ORDER BY colocacao.date_create DESC NULLS LAST, colocacao.id DESC
                FETCH FIRST 1 ROWS ONLY
                """;
        return empregoJdbcTemplate.query(sql, this::mapearColocacao, pessoaId)
                .stream()
                .findFirst();
    }

    public Integer inserir(
            ColocacaoAtiva colocacao,
            MinhaAssiduidadeRequest request,
            String estado
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO emprego_t_assiduidade (
                                id_colocacao,
                                entidade_id,
                                denominacao_entidade,
                                pessoa_id,
                                nome,
                                data,
                                hora_entrada,
                                hora_saida,
                                tipo_assiduidade,
                                justificacao,
                                estado,
                                date_create,
                                user_create
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                            """,
                    new String[]{"id"}
            );
            statement.setInt(1, colocacao.colocacaoId());
            setInteger(statement, 2, colocacao.entidadeId());
            statement.setString(3, colocacao.denominacaoEntidade());
            statement.setLong(4, colocacao.pessoaId());
            statement.setString(5, colocacao.nome());
            statement.setObject(6, request.data());
            statement.setObject(7, request.horaEntrada());
            statement.setObject(8, request.horaSaida());
            statement.setString(9, request.tipoAssiduidade());
            statement.setString(10, request.justificacao());
            statement.setString(11, estado);
            statement.setString(12, request.utilizador());
            return statement;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public boolean atualizar(
            Integer assiduidadeId,
            Long pessoaId,
            MinhaAssiduidadeRequest request
    ) {
        int atualizados = empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_assiduidade
                        SET tipo_assiduidade = ?,
                            data = ?,
                            hora_entrada = ?,
                            hora_saida = ?,
                            justificacao = ?,
                            date_update = CURRENT_TIMESTAMP,
                            user_update = ?
                        WHERE id = ?
                          AND pessoa_id = ?
                        """,
                request.tipoAssiduidade(),
                request.data(),
                request.horaEntrada(),
                request.horaSaida(),
                request.justificacao(),
                request.utilizador(),
                assiduidadeId,
                pessoaId
        );
        return atualizados == 1;
    }

    public boolean atualizarComprovativo(Integer assiduidadeId, Long pessoaId, String comprovativo) {
        int atualizados = empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_assiduidade
                        SET comprovativo = ?
                        WHERE id = ?
                          AND pessoa_id = ?
                        """,
                comprovativo,
                assiduidadeId,
                pessoaId
        );
        return atualizados == 1;
    }

    private String construirWhere(MinhaAssiduidadeFiltro filtro, List<Object> parametros) {
        StringBuilder where = new StringBuilder("WHERE assiduidade.pessoa_id = ?\n");
        parametros.add(filtro.pessoaId());
        if (temTexto(filtro.tipoAssiduidade())) {
            where.append("AND UPPER(assiduidade.tipo_assiduidade) = UPPER(?)\n");
            parametros.add(filtro.tipoAssiduidade());
        }
        if (temTexto(filtro.estado())) {
            where.append("AND UPPER(assiduidade.estado) = UPPER(?)\n");
            parametros.add(filtro.estado());
        }
        if (filtro.dataInicio() != null) {
            where.append("AND assiduidade.date_create >= ?\n");
            parametros.add(filtro.dataInicio().atStartOfDay());
        }
        if (filtro.dataFim() != null) {
            where.append("AND assiduidade.date_create < ?\n");
            parametros.add(filtro.dataFim().plusDays(1).atStartOfDay());
        }
        return where.toString();
    }

    private AssiduidadeRegisto mapearAssiduidade(ResultSet rs, int rowNum) throws SQLException {
        return new AssiduidadeRegisto(
                rs.getInt("id"),
                rs.getObject("id_colocacao", Integer.class),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("denominacao_entidade"),
                getLong(rs, "pessoa_id"),
                rs.getString("nome"),
                rs.getObject("data", LocalDate.class),
                rs.getObject("hora_entrada", LocalTime.class),
                rs.getObject("hora_saida", LocalTime.class),
                rs.getString("tipo_assiduidade"),
                rs.getString("justificacao"),
                rs.getString("estado"),
                rs.getString("observacao"),
                rs.getString("comprovativo"),
                rs.getObject("date_create", LocalDateTime.class),
                rs.getString("user_create"),
                rs.getObject("date_update", LocalDateTime.class),
                rs.getString("user_update")
        );
    }

    private ColocacaoAtiva mapearColocacao(ResultSet rs, int rowNum) throws SQLException {
        return new ColocacaoAtiva(
                rs.getInt("id"),
                rs.getObject("entidade_id", Integer.class),
                rs.getString("denominacao_entidade"),
                getLong(rs, "pessoa_id"),
                rs.getString("nome")
        );
    }

    private Long getLong(ResultSet rs, String coluna) throws SQLException {
        Object valor = rs.getObject(coluna);
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        return Long.valueOf(valor.toString());
    }

    private void setInteger(PreparedStatement statement, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            statement.setNull(indice, Types.INTEGER);
        } else {
            statement.setInt(indice, valor);
        }
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    public record AssiduidadeRegisto(
            Integer assiduidadeId,
            Integer colocacaoId,
            Integer entidadeId,
            String denominacaoEntidade,
            Long pessoaId,
            String nome,
            LocalDate data,
            LocalTime horaEntrada,
            LocalTime horaSaida,
            String tipoAssiduidade,
            String justificacao,
            String estado,
            String observacao,
            String comprovativo,
            LocalDateTime dateCreate,
            String userCreate,
            LocalDateTime dateUpdate,
            String userUpdate
    ) {
    }

    public record ColocacaoAtiva(
            Integer colocacaoId,
            Integer entidadeId,
            String denominacaoEntidade,
            Long pessoaId,
            String nome
    ) {
    }
}
