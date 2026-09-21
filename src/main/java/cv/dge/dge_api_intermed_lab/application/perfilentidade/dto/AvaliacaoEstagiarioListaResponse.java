package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AvaliacaoEstagiarioListaResponse(
        Integer id,
        Long pessoaId,
        String estagiario,
        String tipoAvaliacao,
        String tipoAvaliacaoDescricao,
        String periodoReferencia,
        BigDecimal classificacao,
        LocalDate dataRegisto
) {
}
