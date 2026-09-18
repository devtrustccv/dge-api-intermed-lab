package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record AvaliacaoEstagiarioFiltro(
        Integer entidadeId,
        String estagiario,
        String tipoAvaliacao,
        String periodoReferencia,
        LocalDate dataRegistro,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
