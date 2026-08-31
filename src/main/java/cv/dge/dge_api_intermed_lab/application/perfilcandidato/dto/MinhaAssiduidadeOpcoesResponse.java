package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.util.List;

public record MinhaAssiduidadeOpcoesResponse(
        List<MinhaCandidaturaOpcaoResponse> tiposAssiduidade,
        List<MinhaCandidaturaOpcaoResponse> estadosAssiduidade
) {
}
