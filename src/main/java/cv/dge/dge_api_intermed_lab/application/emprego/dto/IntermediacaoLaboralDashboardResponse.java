package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.math.BigDecimal;
import java.util.List;

public record IntermediacaoLaboralDashboardResponse(
        Long totalOfertasRegistadas,
        Long totalCandidaturasRecebidas,
        BigDecimal mediaVagasPorOferta,
        Long totalEstagiariosSelecionados,
        Long totalEstagiariosAvaliados,
        List<DashboardGrupoResponse> ofertasPorEstado,
        List<DashboardGrupoResponse> ofertasPorTipo,
        List<DashboardGrupoResponse> candidaturasPorEstado,
        List<DashboardResumoOfertaResponse> resumoOfertas
) {
}
