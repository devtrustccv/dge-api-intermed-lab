package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record CandidaturaFiltro(
        Long candidatoId,
        String estado,
        String tipoOferta,
        Integer ofertaId,
        String canal,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
