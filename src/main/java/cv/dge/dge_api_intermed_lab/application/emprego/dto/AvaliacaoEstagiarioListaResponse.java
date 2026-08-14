package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvaliacaoEstagiarioListaResponse(
        Integer id,
        Long pessoaId,
        String estagiario,
        String tipoAvaliacao,
        String tipoAvaliacaoDescricao,
        String periodoReferencia,
        BigDecimal classificacao,
        LocalDateTime dataRegisto
) {
}
