package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import java.util.List;

public record AcolhimentoEntidadeResponse(
        Integer idEntidade,
        Integer total,
        List<AcolhimentoCompletoResponse> acolhimentos
) {
}
