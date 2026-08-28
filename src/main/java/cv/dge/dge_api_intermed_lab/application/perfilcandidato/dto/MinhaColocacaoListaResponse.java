package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDateTime;

public record MinhaColocacaoListaResponse(
        Integer colocacaoId,
        Integer ofertaId,
        String tipoOferta,
        String tipoOfertaDescricao,
        String titulo,
        String codigoReferencia,
        LocalDateTime dataColocacao,
        String contratoPath,
        String contratoUrl
) {
}
