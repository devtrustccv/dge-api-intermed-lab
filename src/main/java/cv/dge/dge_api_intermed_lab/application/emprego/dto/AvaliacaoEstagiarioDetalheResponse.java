package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AvaliacaoEstagiarioDetalheResponse(
        Integer id,
        Long pessoaId,
        String estagiario,
        Integer candidaturaId,
        String tipoAvaliacao,
        String tipoAvaliacaoDescricao,
        String periodoReferencia,
        List<AvaliacaoDesempenhoResponse> avaliacaoDesempenho,
        String grauSatisfacao,
        String grauSatisfacaoDescricao,
        String interesseContratacao,
        BigDecimal classificacao,
        String observacao,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
