package cv.dge.dge_api_intermed_lab.application.emprego.dto;

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
