package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;

public record ConsultaVagaListaResponse(
        Integer id,
        String titulo,
        String tipoOferta,
        String tipoOfertaDesc,
        String ilha,
        String ilhaDesc,
        String concelho,
        String concelhoDesc,
        String localOferta,
        Integer numVagas,
        Integer entidadeId,
        String denominacaoEntidade,
        String codigoReferencia,
        String estado,
        String estadoDesc,
        LocalDate dataInicioCandidatura,
        LocalDate dataFimCandidatura,
        Long diasRestantes,
        String situacao,
        String situacaoDesc,
        Boolean jaCandidatado,
        Boolean podeCandidatar,
        String motivoIndisponibilidade
) {
}
