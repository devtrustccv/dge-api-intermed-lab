package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.dashboard;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.alerta.AreaGeograficaProcuradaResponse;

import java.util.List;

public record PerfilCandidatoDashboardResponse(
        Integer ano,
        Long totalColocacoes,
        Long totalCandidaturas,
        Long totalVagasAbertasEmprego,
        Long totalVagasAbertasEstagio,
        List<EvolucaoCandidaturaMensalResponse> evolucaoCandidaturas,
        List<CandidaturaPorTipoResponse> candidaturasPorTipo,
        List<AreaGeograficaProcuradaResponse> topAreasGeograficas,
        List<ColocacaoRecenteResponse> colocacoesRecentes
) {
}
