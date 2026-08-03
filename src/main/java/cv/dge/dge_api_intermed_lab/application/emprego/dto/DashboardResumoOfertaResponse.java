package cv.dge.dge_api_intermed_lab.application.emprego.dto;

public record DashboardResumoOfertaResponse(
        Integer idOferta,
        String oferta,
        String tipo,
        Integer totalVagas,
        Long totalCandidaturas,
        Long totalCandidaturasAprovadas,
        Long totalEstagiariosAvaliados
) {
}
