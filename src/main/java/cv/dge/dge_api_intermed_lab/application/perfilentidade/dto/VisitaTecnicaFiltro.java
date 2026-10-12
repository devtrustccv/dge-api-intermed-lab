package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record VisitaTecnicaFiltro(
        Integer entidadeId,
        String estado,
        String agendadoPor,
        Integer cefpId,
        String cefp,
        LocalDate dataVisita,
        LocalDate dataInicio,
        LocalDate dataFim,
        String pesquisa
) {
    public VisitaTecnicaFiltro(
            Integer entidadeId, String estado, String agendadoPor, Integer cefpId,
            String cefp, LocalDate dataVisita, LocalDate dataInicio, LocalDate dataFim
    ) {
        this(entidadeId, estado, agendadoPor, cefpId, cefp,
                dataVisita, dataInicio, dataFim, null);
    }
}
