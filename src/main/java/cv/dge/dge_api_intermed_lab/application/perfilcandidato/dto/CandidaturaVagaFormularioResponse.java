package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

public record CandidaturaVagaFormularioResponse(
        Integer vagaId,
        String codigoReferencia,
        String tituloOferta,
        Long pessoaId,
        CandidaturaDocumentoResponse curriculumVitaeAtual,
        String habilitacaoAcademica,
        String areaFormacao,
        Boolean jaCandidatado,
        Integer candidaturaId
) {
}
