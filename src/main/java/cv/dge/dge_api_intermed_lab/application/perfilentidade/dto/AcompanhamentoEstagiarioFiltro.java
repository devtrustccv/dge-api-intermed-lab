package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record AcompanhamentoEstagiarioFiltro(
        Integer entidadeId,
        Long estagiarioId,
        String estagiario,
        Integer ofertaId,
        String oferta
) {
}
