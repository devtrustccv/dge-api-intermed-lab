package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDateTime;

public record AlertaOfertaListaResponse(
        Integer alertaId,
        String tipoOferta,
        String tipoOfertaDescricao,
        String habilitacaoLiteraria,
        String habilitacaoLiterariaDescricao,
        String nivelQualificacao,
        String nivelQualificacaoDescricao,
        String estado,
        String estadoDescricao,
        LocalDateTime dataConfiguracao
) {
}
