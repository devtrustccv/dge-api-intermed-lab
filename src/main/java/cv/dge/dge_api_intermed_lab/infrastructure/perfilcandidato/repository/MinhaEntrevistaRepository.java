package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MinhaEntrevistaRepository {

    private final JdbcTemplate empregoJdbcTemplate;

    public MinhaEntrevistaRepository(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
    }

    public List<EntrevistaRegisto> listar(Long pessoaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT
                            entrevista.id AS entrevista_id,
                            entrevista.id_acolhimento AS acolhimento_id,
                            entrevista.data_encaminhamento,
                            entrevista.data_entrevista,
                            entrevista.hora_inicio,
                            entrevista.hora_fim,
                            entrevista.nome_tecnico,
                            entrevista.local,
                            entrevista.dm_status_entrevista AS estado,
                            entrevista.id_cefp AS cefp_id,
                            entrevista.cefp,
                            entrevista.tipo_servico,
                            entrevista.canal,
                            entrevista.local_entrevista
                        FROM emprego_t_agendamento_entrevista entrevista
                        INNER JOIN emprego_t_utente utente
                            ON utente.id = entrevista.id_utente
                        WHERE CAST(utente.pessoa_id AS BIGINT) = ?
                        ORDER BY entrevista.data_entrevista DESC NULLS LAST,
                                 entrevista.hora_inicio DESC NULLS LAST,
                                 entrevista.id DESC
                        """,
                this::mapEntrevista,
                pessoaId
        );
    }

    private EntrevistaRegisto mapEntrevista(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return new EntrevistaRegisto(
                rs.getInt("entrevista_id"),
                rs.getObject("acolhimento_id", Integer.class),
                rs.getObject("data_encaminhamento", LocalDate.class),
                rs.getObject("data_entrevista", LocalDate.class),
                rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fim", LocalTime.class),
                rs.getString("nome_tecnico"),
                rs.getString("local"),
                rs.getString("estado"),
                rs.getObject("cefp_id", Integer.class),
                rs.getString("cefp"),
                rs.getString("tipo_servico"),
                rs.getString("canal"),
                rs.getString("local_entrevista")
        );
    }

    public record EntrevistaRegisto(
            Integer entrevistaId,
            Integer acolhimentoId,
            LocalDate dataEncaminhamento,
            LocalDate dataEntrevista,
            LocalTime horaInicio,
            LocalTime horaFim,
            String nomeTecnico,
            String local,
            String estado,
            Integer cefpId,
            String cefp,
            String tipoServico,
            String canal,
            String localEntrevista
    ) {
    }
}
