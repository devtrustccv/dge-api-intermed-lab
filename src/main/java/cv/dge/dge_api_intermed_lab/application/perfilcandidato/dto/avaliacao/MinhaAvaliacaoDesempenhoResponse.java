package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.avaliacao;

public record MinhaAvaliacaoDesempenhoResponse(
        String tipoCompetencia,
        String tipoCompetenciaDescricao,
        String avaliacao,
        String avaliacaoDescricao
) {
}
