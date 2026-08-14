package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.math.BigDecimal;
import java.util.List;

public record AvaliacaoEstagiarioRequest(
        Long pessoaId,
        String tipoAvaliacao,
        String periodoReferencia,
        List<AvaliacaoDesempenhoRequest> avaliacaoDesempenho,
        String grauSatisfacao,
        String interesseContratacao,
        BigDecimal classificacao,
        String observacao,
        String utilizador
) {
}
