package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record VisitaTecnicaListaResponse(
        Integer id,
        Integer entidadeId,
        LocalDate dataVisita,
        LocalTime horaInicio,
        LocalTime horaFim,
        String horario,
        String visitante,
        String objetivos,
        String agendadoPor,
        String agendadoPorDesc,
        Integer cefpId,
        String cefp,
        String estado,
        String estadoDesc,
        List<VisitaTecnicaCandidatoRequest> candidatos,
        LocalDateTime novaData,
        String motivoIndeferimento,
        String observacoesEntidade,
        String supervisorParticipante,
        String observacoesIefp,
        List<VisitaTecnicaAvaliacaoItemRequest> detalhesAvaliacao,
        String conteudoReuniao,
        Boolean podeValidar,
        Boolean podeMarcarComoExecutado,
        Boolean podeRegistarObservacoes,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
