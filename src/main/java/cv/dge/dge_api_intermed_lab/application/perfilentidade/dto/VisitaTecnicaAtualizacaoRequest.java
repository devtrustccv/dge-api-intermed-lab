package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record VisitaTecnicaAtualizacaoRequest(
        LocalDate dataVisita,
        String visitante,
        LocalTime horaInicio,
        LocalTime horaFim,
        String objetivos,
        List<VisitaTecnicaCandidatoRequest> candidatos,
        String observacoesEntidade,
        String supervisorParticipante,
        String observacoesIefp,
        List<VisitaTecnicaAvaliacaoItemRequest> detalhesAvaliacao,
        String utilizador
) {
}
