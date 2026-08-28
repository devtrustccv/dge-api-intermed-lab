package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDateTime;

public record MinhaCandidaturaListaResponse(
        Integer candidaturaId,
        String tipoOferta,
        String tipoOfertaDescricao,
        Integer ofertaId,
        String titulo,
        String codigoReferencia,
        Integer entidadeId,
        String entidade,
        String ilhaId,
        String ilha,
        String concelhoId,
        String concelho,
        String estado,
        String estadoDescricao,
        LocalDateTime dataCandidatura
) {
}
