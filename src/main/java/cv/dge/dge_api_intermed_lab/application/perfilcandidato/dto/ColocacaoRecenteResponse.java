package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDateTime;

public record ColocacaoRecenteResponse(
        Integer idColocacao,
        Integer idOferta,
        String nomeOferta,
        String nomeEmpresa,
        String ilha,
        String concelho,
        String tipoOferta,
        String tipoOfertaDescricao,
        LocalDateTime dataColocacao
) {
}
