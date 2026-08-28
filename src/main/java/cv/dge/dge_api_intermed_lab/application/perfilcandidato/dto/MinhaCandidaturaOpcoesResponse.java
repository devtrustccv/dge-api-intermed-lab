package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.util.List;

public record MinhaCandidaturaOpcoesResponse(
        List<MinhaCandidaturaOpcaoResponse> tiposOferta,
        List<ConsultaVagaOpcaoResponse> entidades,
        List<ConsultaVagaOpcaoResponse> ilhas,
        List<ConsultaVagaOpcaoResponse> concelhos,
        List<MinhaCandidaturaOpcaoResponse> estadosCandidatura
) {
}
