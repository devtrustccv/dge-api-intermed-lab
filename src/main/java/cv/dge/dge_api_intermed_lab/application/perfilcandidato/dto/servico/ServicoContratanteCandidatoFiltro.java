package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.servico;

import java.time.LocalDate;

public record ServicoContratanteCandidatoFiltro(
        Integer servicoId,
        Long contratanteId,
        Long candidatoId,
        String estado,
        LocalDate dataInicio,
        LocalDate dataFim,
        String search
) {
    public ServicoContratanteCandidatoFiltro(
            Integer servicoId,
            Long contratanteId,
            Long candidatoId,
            String estado,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        this(servicoId, contratanteId, candidatoId, estado, dataInicio, dataFim, null);
    }
}
