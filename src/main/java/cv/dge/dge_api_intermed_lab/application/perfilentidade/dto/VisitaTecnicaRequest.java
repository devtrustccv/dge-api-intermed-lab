package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record VisitaTecnicaRequest(
        Integer entidadeId,
        LocalDate dataVisita,
        String visitante,
        List<VisitaTecnicaCandidatoRequest> candidatos,
        LocalTime horaInicio,
        LocalTime horaFim,
        String objetivos,
        Integer cefpId,
        String cefp,
        String utilizador
) {
}
