package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;

public record ServicoContratanteCandidatoFiltro(
        Integer servicoId,
        Long contratanteId,
        Long candidatoId,
        String estado,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
