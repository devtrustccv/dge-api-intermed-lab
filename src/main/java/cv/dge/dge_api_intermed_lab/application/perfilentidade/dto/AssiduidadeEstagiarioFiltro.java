package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record AssiduidadeEstagiarioFiltro(
        Integer entidadeId,
        Long estagiarioId,
        Integer ofertaId,
        String tipoAssiduidade,
        String estado
) {
}
