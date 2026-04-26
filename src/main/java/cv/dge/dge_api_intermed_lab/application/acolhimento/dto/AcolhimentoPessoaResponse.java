package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import java.util.List;

public record AcolhimentoPessoaResponse(
        Integer idPessoa,
        Integer total,
        List<AcolhimentoCompletoResponse> acolhimentos
) {
}
