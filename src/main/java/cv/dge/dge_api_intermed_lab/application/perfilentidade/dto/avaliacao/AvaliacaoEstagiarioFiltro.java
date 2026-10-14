package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.avaliacao;

import java.time.LocalDate;

public record AvaliacaoEstagiarioFiltro(
        Integer entidadeId,
        String estagiario,
        String tipoAvaliacao,
        String periodoReferencia,
        LocalDate dataRegistro,
        LocalDate dataInicio,
        LocalDate dataFim,
        String search
) {
    public AvaliacaoEstagiarioFiltro(
            Integer entidadeId, String estagiario, String tipoAvaliacao,
            String periodoReferencia, LocalDate dataRegistro,
            LocalDate dataInicio, LocalDate dataFim
    ) {
        this(entidadeId, estagiario, tipoAvaliacao, periodoReferencia,
                dataRegistro, dataInicio, dataFim, null);
    }
}
