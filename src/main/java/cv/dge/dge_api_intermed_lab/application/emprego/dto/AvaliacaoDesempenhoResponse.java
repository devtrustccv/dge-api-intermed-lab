package cv.dge.dge_api_intermed_lab.application.emprego.dto;

public record AvaliacaoDesempenhoResponse(
        String tipoCompetencia,
        String tipoCompetenciaDescricao,
        String avaliacao,
        String avaliacaoDescricao
) {
}
