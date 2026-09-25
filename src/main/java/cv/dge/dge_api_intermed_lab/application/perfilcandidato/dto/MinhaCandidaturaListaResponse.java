package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("Motivo Recusa") String motivoRecusa,
        @JsonProperty("Canal") String canal,
        @JsonProperty("Tipo Documento") String tipoDocumento,
        @JsonProperty("Preview") String preview,
        LocalDateTime dataCandidatura
) {
}
