package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.util.List;

public record AlertaOfertaOpcoesResponse(
        Long pessoaId,
        String pessoaNome,
        List<MinhaCandidaturaOpcaoResponse> tiposOferta,
        List<ConsultaVagaOpcaoResponse> ilhas,
        List<ConsultaVagaOpcaoResponse> concelhos,
        List<ConsultaVagaOpcaoResponse> entidades,
        List<MinhaCandidaturaOpcaoResponse> habilitacoesLiterarias,
        List<MinhaCandidaturaOpcaoResponse> niveisQualificacao
) {
}
