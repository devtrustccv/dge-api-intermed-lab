package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.candidatura;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.candidatura.CandidaturaAvaliacaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.entrevista.EntrevistaResponse;
import java.time.LocalDateTime;
import java.util.List;

public record MinhaCandidaturaDetalheResponse(
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
        String motivoRecusa,
        String canal,
        String canalDescricao,
        LocalDateTime dataCandidatura,
        List<CandidaturaDocumentoResponse> anexos,
        CandidaturaAvaliacaoResponse avaliacao,
        List<EntrevistaResponse> entrevistas
) {
}
