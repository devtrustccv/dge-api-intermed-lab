package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MinhaEntrevistaListaResponse(
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
