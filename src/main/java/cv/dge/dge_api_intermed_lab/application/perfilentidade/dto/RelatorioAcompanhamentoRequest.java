package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record RelatorioAcompanhamentoRequest(
        Long pessoaId,
        String codigoReferencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        String atividadesRealizadas,
        String dificuldades,
        String recomendacoes,
        String relatorioAnexo,
        String utilizador
) {
}
