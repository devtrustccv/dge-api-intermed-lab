package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MinhaAvaliacaoListaResponse(
        Integer avaliacaoId,
        String tipoAvaliacao,
        String tipoAvaliacaoDescricao,
        String periodoReferencia,
        BigDecimal classificacao,
        LocalDateTime dataRegisto
) {
}
