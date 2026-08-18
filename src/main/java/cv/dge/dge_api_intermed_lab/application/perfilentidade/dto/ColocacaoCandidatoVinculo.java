package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record ColocacaoCandidatoVinculo(
        Integer candidaturaId,
        Long pessoaId,
        String nome,
        Integer ofertaId,
        String tipoOferta,
        String codigoReferencia,
        Integer entidadeId,
        String denominacaoEntidade
) {
}
