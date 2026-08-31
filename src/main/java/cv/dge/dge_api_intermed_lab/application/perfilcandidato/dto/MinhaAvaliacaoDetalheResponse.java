package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MinhaAvaliacaoDetalheResponse(
        Integer avaliacaoId,
        String tipoAvaliacao,
        String tipoAvaliacaoDescricao,
        String periodoReferencia,
        List<MinhaAvaliacaoDesempenhoResponse> avaliacaoDesempenho,
        String grauSatisfacao,
        String grauSatisfacaoDescricao,
        String interesseContratacao,
        BigDecimal classificacao,
        String observacao,
        LocalDateTime dataRegisto
) {
}
