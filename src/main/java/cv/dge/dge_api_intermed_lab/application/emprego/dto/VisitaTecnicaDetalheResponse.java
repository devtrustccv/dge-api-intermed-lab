package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record VisitaTecnicaDetalheResponse(
        Integer id,
        Integer entidadeId,
        LocalDate dataVisita,
        String visitante,
        LocalTime horaInicio,
        LocalTime horaFim,
        String horario,
        String objetivos,
        String agendadoPor,
        String agendadoPorDesc,
        Integer cefpId,
        String cefp,
        String estado,
        String estadoDesc,
        Object candidatos,
        LocalDate novaData,
        String motivoIndeferimento,
        String observacoesEntidade,
        String supervisorParticipante,
        String observacoesIefp,
        Object detalhesAvaliacao,
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
