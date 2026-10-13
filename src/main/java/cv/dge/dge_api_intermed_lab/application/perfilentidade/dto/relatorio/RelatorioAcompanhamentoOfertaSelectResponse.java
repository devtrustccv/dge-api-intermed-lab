package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio;

import java.util.List;

public record RelatorioAcompanhamentoOfertaSelectResponse(
        Integer ofertaId,
        String codigoReferencia,
        String titulo,
        String oferta,
        List<RelatorioAcompanhamentoEstagiarioSelectResponse> estagiarios
) {
}
