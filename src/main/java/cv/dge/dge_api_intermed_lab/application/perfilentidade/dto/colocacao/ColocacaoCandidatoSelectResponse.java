package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao;

public record ColocacaoCandidatoSelectResponse(
        Long pessoaId,
        String nome,
        Integer candidaturaId,
        Integer ofertaId,
        String codigoReferencia
) {
}
