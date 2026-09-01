package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.util.List;

public record ServicoContratanteOpcoesResponse(
        List<MinhaCandidaturaOpcaoResponse> estados,
        List<ConsultaVagaOpcaoResponse> ilhas,
        List<ConsultaVagaOpcaoResponse> concelhos,
        List<ConsultaVagaOpcaoResponse> zonas
) {
}
