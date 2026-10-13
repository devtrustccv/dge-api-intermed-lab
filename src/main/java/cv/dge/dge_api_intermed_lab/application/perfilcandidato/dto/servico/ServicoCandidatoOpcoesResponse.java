package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.servico;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.candidatura.MinhaCandidaturaOpcaoResponse;

import java.util.List;

public record ServicoCandidatoOpcoesResponse(
        List<MinhaCandidaturaOpcaoResponse> estadosServico
) {
}
