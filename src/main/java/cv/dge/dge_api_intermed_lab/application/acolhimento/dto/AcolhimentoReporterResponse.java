package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record AcolhimentoReporterResponse(
        Integer idAcolhimento,
        String logotipoIefp,
        String logotipoDge,
        String nomeCefp,
        String enderecoCefp,
        String telefoneCefp,
        String emailCefp,
        LocalDateTime data,
        String numeroRegisto,
        Integer numeroUtente,
        String nomeUtente,
        String tecnicoAtendimento,
        CefpReporterDTO cefp,
        UtenteReporterDTO utente,
        EntidadeReporterDTO entidade,
        Map<String, Object> detalhes
) {
}
