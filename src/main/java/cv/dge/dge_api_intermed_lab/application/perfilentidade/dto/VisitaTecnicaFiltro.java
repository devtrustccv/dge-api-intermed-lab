package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record VisitaTecnicaFiltro(
        Integer entidadeId,
        String estado,
        String agendadoPor,
        Integer cefpId,
        LocalDate dataVisita,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
