package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemResponse;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PerfilCandidatoAdesaoRepository {

    private final JdbcTemplate empregoJdbcTemplate;

    public PerfilCandidatoAdesaoRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
    }

    public Optional<Integer> buscarUtenteId(Long pessoaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT id
                        FROM emprego_t_utente
                        WHERE CAST(pessoa_id AS BIGINT) = ?
                        ORDER BY id DESC
                        FETCH FIRST 1 ROWS ONLY
                        """,
                (rs, rowNum) -> rs.getInt("id"),
                pessoaId
        ).stream().findFirst();
    }

    public boolean existeAdesao(Long pessoaId) {
        Long total = empregoJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM emprego_t_adesao WHERE pessoa_id = ?",
                Long.class,
                pessoaId
        );
        return total != null && total > 0;
    }

    public AdesaoJovemResponse inserir(
            Long pessoaId,
            Integer utenteId,
            String situacaoProfissional,
            String utilizador
    ) {
        return empregoJdbcTemplate.queryForObject(
                """
                        INSERT INTO emprego_t_adesao (
                            pessoa_id, situacao_profissional, id_utente, date_create, user_create
                        ) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)
                        RETURNING id, pessoa_id, id_utente, situacao_profissional, date_create, user_create
                        """,
                (rs, rowNum) -> new AdesaoJovemResponse(
                        rs.getInt("id"),
                        rs.getLong("pessoa_id"),
                        rs.getInt("id_utente"),
                        rs.getString("situacao_profissional"),
                        rs.getTimestamp("date_create").toLocalDateTime(),
                        rs.getString("user_create")
                ),
                pessoaId,
                situacaoProfissional,
                utenteId,
                utilizador
        );
    }
}
