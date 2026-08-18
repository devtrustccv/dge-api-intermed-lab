package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record RelatorioAcompanhamentoFiltro(
        Integer entidadeId,
        Long pessoaId,
        String codigoReferencia,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
