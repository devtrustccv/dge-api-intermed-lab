package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDateTime;

public record RelatorioAcompanhamentoListaResponse(
        Integer id,
        Long pessoaId,
        String estagiario,
        Integer ofertaId,
        String codigoReferencia,
        LocalDateTime dataRegisto,
        String relatorioAnexo,
        String estado,
        String estadoDescricao
) {
}
