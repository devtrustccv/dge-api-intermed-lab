package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.acompanhamento;

public record AcompanhamentoEstagiarioFiltro(
        Integer entidadeId,
        Long estagiarioId,
        String estagiario,
        Integer ofertaId,
        String oferta,
        String search
) {
    public AcompanhamentoEstagiarioFiltro(
            Integer entidadeId, Long estagiarioId, String estagiario,
            Integer ofertaId, String oferta
    ) {
        this(entidadeId, estagiarioId, estagiario, ofertaId, oferta, null);
    }
}
