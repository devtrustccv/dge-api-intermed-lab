package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record AvaliacaoEstagiarioFiltro(
        Integer entidadeId,
        Long pessoaId,
        String tipoAvaliacao,
        String periodoReferencia,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
