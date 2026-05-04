package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import java.util.Map;

public record AcolhimentoEmpresaResponse(
        Integer idAcolhimento,
        Integer idEntidade,
        String numInscricao,
        Integer cefpId,
        Integer orgId,
        Map<String, Object> detalhes
) {
}
