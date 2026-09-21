package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record CandidaturaFiltro(
        Integer entidadeId,
        Long candidatoId,
        String candidato,
        String estado,
        String tipoOferta,
        Integer ofertaId,
        String canal,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
