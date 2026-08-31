package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

public record AlertaOfertaRequest(
        String tipoOferta,
        String ilha,
        String concelho,
        Integer entidadeId,
        String habilitacaoLiteraria,
        String nivelQualificacao,
        String utilizador
) {
}
