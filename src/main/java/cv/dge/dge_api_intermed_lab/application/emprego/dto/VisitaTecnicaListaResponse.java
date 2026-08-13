package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record VisitaTecnicaListaResponse(
        Integer id,
        Integer entidadeId,
        LocalDate dataVisita,
        LocalTime horaInicio,
        LocalTime horaFim,
        String horario,
        String visitante,
        String agendadoPor,
        String agendadoPorDesc,
        Integer cefpId,
        String cefp,
        String estado,
        String estadoDesc,
        Boolean podeValidar,
        Boolean podeMarcarComoExecutado,
        Boolean podeRegistarObservacoes
) {
}
