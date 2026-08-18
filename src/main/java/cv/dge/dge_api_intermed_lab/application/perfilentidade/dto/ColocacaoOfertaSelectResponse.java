package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record ColocacaoOfertaSelectResponse(
        Integer ofertaId,
        String codigoReferencia,
        String titulo,
        String oferta,
        String tipoOferta,
        String tipoOfertaDesc,
        Integer entidadeId,
        String denominacaoEntidade
) {
}
