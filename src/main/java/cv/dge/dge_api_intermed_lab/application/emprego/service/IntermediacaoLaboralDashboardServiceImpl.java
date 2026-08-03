package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.IntermediacaoLaboralDashboardResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.emprego.repository.IntermediacaoLaboralDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IntermediacaoLaboralDashboardServiceImpl implements IntermediacaoLaboralDashboardService {

    private final IntermediacaoLaboralDashboardRepository dashboardRepository;

    @Override
    @Transactional(readOnly = true)
    public IntermediacaoLaboralDashboardResponse buscarResumo(Integer entidadeId) {
        return new IntermediacaoLaboralDashboardResponse(
                dashboardRepository.contarOfertas(entidadeId),
                dashboardRepository.contarCandidaturas(entidadeId),
                dashboardRepository.calcularMediaVagasPorOferta(entidadeId),
                dashboardRepository.contarEstagiariosSelecionados(entidadeId),
                dashboardRepository.contarEstagiariosAvaliados(entidadeId),
                dashboardRepository.listarOfertasPorEstado(entidadeId),
                dashboardRepository.listarOfertasPorTipo(entidadeId),
                dashboardRepository.listarCandidaturasPorEstado(entidadeId),
                dashboardRepository.listarResumoOfertas(entidadeId)
        );
    }
}
