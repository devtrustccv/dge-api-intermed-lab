package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemResponse;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PerfilCandidatoAdesaoRepository {

    private static final DateTimeFormatter DATA_DIA_MES_ANO = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final JdbcTemplate empregoJdbcTemplate;

    public PerfilCandidatoAdesaoRepository(
            @Qualifier("primaryDataSource") DataSource primaryDataSource
    ) {
        this.empregoJdbcTemplate = new JdbcTemplate(primaryDataSource);
    }

    public Optional<AdesaoJovemResponse> buscarFormulario(Long pessoaId) {
        Optional<UtenteLocal> utenteLocal = buscarUtenteLocal(pessoaId);
        if (utenteLocal.isEmpty()) {
            return Optional.empty();
        }

        UtenteLocal utente = utenteLocal.orElseThrow();
        AdesaoRegisto adesao = buscarAdesao(pessoaId).orElse(null);

        return Optional.of(new AdesaoJovemResponse(
                adesao == null ? null : adesao.id(),
                pessoaId,
                utente.foto(),
                utente.nif(),
                utente.nacionalidade(),
                utente.dataNascimento(),
                utente.sexo(),
                utente.tipoDocumento(),
                utente.numeroDocumento(),
                utente.localEmissao(),
                utente.dataValidade(),
                utente.estadoCivil(),
                adesao == null ? null : adesao.situacaoProfissional(),
                adesao != null,
                adesao == null,
                adesao == null ? null : adesao.dataAdesao(),
                adesao == null ? null : adesao.utilizadorRegisto()
        ));
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

    public Integer inserir(Long pessoaId, Integer utenteId, String situacaoProfissional, String utilizador) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        empregoJdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                            INSERT INTO emprego_t_adesao (
                                pessoa_id, situacao_profissional, id_utente, date_create, user_create
                            ) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)
                            """,
                    new String[]{"id"}
            );
            statement.setLong(1, pessoaId);
            statement.setString(2, situacaoProfissional);
            statement.setInt(3, utenteId);
            statement.setString(4, utilizador);
            return statement;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("Não foi possível obter o identificador da adesão registada.");
        }
        return id.intValue();
    }

    private Optional<UtenteLocal> buscarUtenteLocal(Long pessoaId) {
        List<UtenteLocal> resultados = empregoJdbcTemplate.query(
                """
                        SELECT
                            utente.id,
                            CAST(utente.nif AS VARCHAR) AS nif,
                            utente.data_nascimento,
                            utente.sexo,
                            utente.tipo_documento,
                            utente.num_documento,
                            detalhes.dados ->> 'link_foto' AS foto,
                            COALESCE(
                                NULLIF(TRIM(detalhes.dados ->> 'nacionalidade'), ''),
                                NULLIF(TRIM(detalhes.dados ->> 'naturalidade'), '')
                            ) AS nacionalidade,
                            detalhes.dados ->> 'local_de_emissao' AS local_emissao,
                            detalhes.dados ->> 'data_validade' AS data_validade,
                            detalhes.dados ->> 'estado_civil' AS estado_civil
                        FROM emprego_t_utente utente
                        LEFT JOIN LATERAL (
                            SELECT acolhimento.detalhes AS dados
                            FROM emprego_t_detalhes_acolhimento acolhimento
                            WHERE acolhimento.id_utente = utente.id
                            ORDER BY acolhimento.date_create DESC NULLS LAST, acolhimento.id DESC
                            FETCH FIRST 1 ROWS ONLY
                        ) detalhes ON TRUE
                        WHERE CAST(utente.pessoa_id AS BIGINT) = ?
                        ORDER BY utente.id DESC
                        FETCH FIRST 1 ROWS ONLY
                        """,
                (rs, rowNum) -> new UtenteLocal(
                        rs.getInt("id"),
                        rs.getString("foto"),
                        rs.getString("nif"),
                        rs.getString("nacionalidade"),
                        rs.getObject("data_nascimento", LocalDate.class),
                        rs.getString("sexo"),
                        rs.getString("tipo_documento"),
                        rs.getString("num_documento"),
                        rs.getString("local_emissao"),
                        converterData(rs.getString("data_validade")),
                        rs.getString("estado_civil")
                ),
                pessoaId
        );
        return resultados.stream().findFirst();
    }

    private Optional<AdesaoRegisto> buscarAdesao(Long pessoaId) {
        return empregoJdbcTemplate.query(
                """
                        SELECT id, situacao_profissional, date_create, user_create
                        FROM emprego_t_adesao
                        WHERE pessoa_id = ?
                        ORDER BY date_create DESC NULLS LAST, id DESC
                        FETCH FIRST 1 ROWS ONLY
                        """,
                (rs, rowNum) -> new AdesaoRegisto(
                        rs.getInt("id"),
                        rs.getString("situacao_profissional"),
                        rs.getObject("date_create", LocalDateTime.class),
                        rs.getString("user_create")
                ),
                pessoaId
        ).stream().findFirst();
    }

    private LocalDate converterData(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        String data = valor.trim();
        try {
            return LocalDate.parse(data);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(data, DATA_DIA_MES_ANO);
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private record UtenteLocal(
            Integer id,
            String foto,
            String nif,
            String nacionalidade,
            LocalDate dataNascimento,
            String sexo,
            String tipoDocumento,
            String numeroDocumento,
            String localEmissao,
            LocalDate dataValidade,
            String estadoCivil
    ) {
    }

    private record AdesaoRegisto(
            Integer id,
            String situacaoProfissional,
            LocalDateTime dataAdesao,
            String utilizadorRegisto
    ) {
    }
}
