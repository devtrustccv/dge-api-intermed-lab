package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MinhaColocacaoDetalheResponse(
        Integer colocacaoId,
        Integer ofertaId,
        String tipoOferta,
        String tipoOfertaDescricao,
        String titulo,
        String codigoReferencia,
        LocalDate dataInicioPrevisto,
        LocalDate dataFimPrevisto,
        String tipoContrato,
        String tipoContratoDescricao,
        Integer duracaoContrato,
        String descricao,
        String estado,
        String estadoDescricao,
        LocalDateTime dataColocacao,
        String contratoPath,
        String contratoUrl
) {
}
