package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import java.util.Map;

public record AcolhimentoRegistoResponse(
        Integer idAcolhimento,
        Integer idUtente,
        Integer idPessoa,
        String numInscricao,
        Integer cefpId,
        Integer orgId,
        Map<String, Object> detalhes
) {
}
