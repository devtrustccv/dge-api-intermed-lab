package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record RelatorioAcompanhamentoFiltro(
        Integer entidadeId,
        Long pessoaId,
        String estagiario,
        String codigoReferencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        String pesquisa
) {
    public RelatorioAcompanhamentoFiltro(
            Integer entidadeId, Long pessoaId, String estagiario,
            String codigoReferencia, LocalDate dataInicio, LocalDate dataFim
    ) {
        this(entidadeId, pessoaId, estagiario, codigoReferencia, dataInicio, dataFim, null);
    }
}
