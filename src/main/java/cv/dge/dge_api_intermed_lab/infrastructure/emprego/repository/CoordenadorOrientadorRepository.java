package cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.PessoaGlobalResponse;
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
public class CoordenadorOrientadorRepository {

    private static final String CAMPOS_COLABORADOR = """
            id,
            tipo,
            nome,
            pessoa_id,
            cargo,
            estado,
            date_create,
            user_create,
            date_update,
            user_update
            """;

    private static final String SQL_INSERT = """
            INSERT INTO emprego_t_entidade_colaborador (
                tipo,
                nome,
                pessoa_id,
                cargo,
                estado,
                date_create,
                user_create
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_UPDATE = """
            UPDATE emprego_t_entidade_colaborador
            SET tipo = ?,
                cargo = ?,
                date_update = ?,
                user_update = ?
            WHERE id = ?
            """;

    private final JdbcTemplate empregoJdbcTemplate;
    private final JdbcTemplate globalJdbcTemplate;

    public CoordenadorOrientadorRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("tertiaryDataSource") DataSource tertiaryDataSource
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.globalJdbcTemplate = new JdbcTemplate(tertiaryDataSource);
    }

    public List<CoordenadorOrientadorListaResponse> listar(CoordenadorOrientadorFiltro filtro) {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(filtro, params);
        String sql = """
                SELECT
                    id,
                    tipo,
                    nome,
                    pessoa_id,
                    estado,
                    date_create,
                    user_create
                FROM emprego_t_entidade_colaborador c
                """ + where + """
                ORDER BY c.date_create DESC NULLS LAST, c.id DESC
                """;

        return empregoJdbcTemplate.query(sql, (rs, rowNum) -> {
            PessoaGlobalResponse pessoa = buscarPessoaPorId(rs.getObject("pessoa_id", Integer.class)).orElse(null);
            return new CoordenadorOrientadorListaResponse(
                    rs.getInt("id"),
                    rs.getString("tipo"),
                    rs.getString("tipo"),
                    rs.getString("nome"),
                    pessoa == null ? null : pessoa.email(),
                    pessoa == null ? null : pessoa.telemovel(),
                    rs.getString("estado"),
                    rs.getString("estado"),
                    rs.getObject("date_create", java.time.LocalDateTime.class),
                    rs.getString("user_create")
            );
        }, params.toArray());
    }

    public Optional<CoordenadorOrientadorResponse> buscarPorId(Integer id) {
        String sql = "SELECT " + CAMPOS_COLABORADOR + " FROM emprego_t_entidade_colaborador c WHERE c.id = ?";
        List<CoordenadorOrientadorResponse> resultados = empregoJdbcTemplate.query(sql, (rs, rowNum) -> {
            Integer pessoaId = rs.getObject("pessoa_id", Integer.class);
            PessoaGlobalResponse pessoa = buscarPessoaPorId(pessoaId).orElse(null);
            return new CoordenadorOrientadorResponse(
                    rs.getInt("id"),
                    rs.getString("tipo"),
                    rs.getString("tipo"),
                    rs.getString("nome"),
                    pessoaId,
                    rs.getString("cargo"),
                    pessoa == null ? null : pessoa.email(),
                    pessoa == null ? null : pessoa.telemovel(),
                    rs.getString("estado"),
                    rs.getString("estado"),
                    rs.getObject("date_create", java.time.LocalDateTime.class),
                    rs.getString("user_create"),
                    rs.getObject("date_update", java.time.LocalDateTime.class),
                    rs.getString("user_update")
            );
        }, id);
        return resultados.stream().findFirst();
    }

    public Optional<PessoaGlobalResponse> buscarPessoaPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        List<PessoaGlobalResponse> resultados = globalJdbcTemplate.query(
                """
                        SELECT id, nome, email, telefone AS telemovel, num_documento
                        FROM ci_t_pessoa
                        WHERE id = ?
                        """,
                (rs, rowNum) -> new PessoaGlobalResponse(
                        getInteger(rs, "id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telemovel"),
                        null,
                        rs.getString("num_documento")
                ),
                id
        );
        return resultados.stream().findFirst();
    }

    public Optional<PessoaGlobalResponse> buscarPessoaPorNumeroDocumento(String numeroDocumento) {
        if (!temTexto(numeroDocumento)) {
            return Optional.empty();
        }
        List<PessoaGlobalResponse> resultados = globalJdbcTemplate.query(
                """
                        SELECT id, nome, email, telefone AS telemovel, num_documento
                        FROM ci_t_pessoa
                        WHERE UPPER(num_documento) = UPPER(?)
                        FETCH FIRST 1 ROWS ONLY
                        """,
                (rs, rowNum) -> new PessoaGlobalResponse(
                        getInteger(rs, "id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telemovel"),
                        null,
                        rs.getString("num_documento")
                ),
                numeroDocumento.trim()
        );
        return resultados.stream().findFirst();
    }

    public Integer inserir(CoordenadorOrientadorRequest request, PessoaGlobalResponse pessoa, String estado, String utilizador) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime agora = LocalDateTime.now();

        empregoJdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setString(1, request.tipo());
            ps.setString(2, pessoa.nome());
            setInteger(ps, 3, pessoa.id());
            ps.setString(4, request.cargo());
            ps.setString(5, estado);
            ps.setTimestamp(6, Timestamp.valueOf(agora));
            ps.setString(7, utilizador);
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return id == null ? null : id.intValue();
    }

    public void atualizar(Integer id, CoordenadorOrientadorRequest request, String utilizador) {
        empregoJdbcTemplate.update(
                SQL_UPDATE,
                request.tipo(),
                request.cargo(),
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    public void remover(Integer id, String utilizador, String estadoInativo) {
        empregoJdbcTemplate.update(
                """
                        UPDATE emprego_t_entidade_colaborador
                        SET estado = ?,
                            date_update = ?,
                            user_update = ?
                        WHERE id = ?
                        """,
                estadoInativo,
                Timestamp.valueOf(LocalDateTime.now()),
                utilizador,
                id
        );
    }

    public void atualizarContactosPessoa(Integer pessoaId, String email, String telemovel) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE ci_t_pessoa SET ");

        if (temTexto(email)) {
            sql.append("email = ?");
            params.add(email.trim());
        }
        if (temTexto(telemovel)) {
            if (!params.isEmpty()) {
                sql.append(", ");
            }
            sql.append("telefone = ?");
            params.add(telemovel.trim());
        }
        if (params.isEmpty()) {
            return;
        }

        sql.append(" WHERE id = ?");
        params.add(pessoaId);
        globalJdbcTemplate.update(sql.toString(), params.toArray());
    }

    private String construirWhere(CoordenadorOrientadorFiltro filtro, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");

        if (temTexto(filtro.nome())) {
            where.append(" AND c.nome ILIKE ?");
            params.add("%" + filtro.nome().trim() + "%");
        }
        if (temTexto(filtro.tipo())) {
            where.append(" AND UPPER(c.tipo) = UPPER(?)");
            params.add(filtro.tipo().trim());
        }
        adicionarFiltroEstado(where, params, filtro.estado());
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

    private void adicionarFiltroEstado(StringBuilder where, List<Object> params, String estado) {
        if (!temTexto(estado)) {
            return;
        }
        String valor = estado.trim().toUpperCase();
        if ("A".equals(valor) || "ATIVO".equals(valor)) {
            where.append(" AND UPPER(c.estado) IN ('A', 'ATIVO')");
            return;
        }
        if ("I".equals(valor) || "INATIVO".equals(valor) || "INACTIVO".equals(valor)) {
            where.append(" AND UPPER(c.estado) IN ('I', 'INATIVO', 'INACTIVO')");
            return;
        }
        where.append(" AND UPPER(c.estado) = UPPER(?)");
        params.add(valor);
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
            return Math.toIntExact(number.longValue());
        }
        return Integer.valueOf(value.toString());
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
