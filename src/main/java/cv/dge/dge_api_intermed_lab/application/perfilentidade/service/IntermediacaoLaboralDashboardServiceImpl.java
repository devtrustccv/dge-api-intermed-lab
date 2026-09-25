package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.DashboardGrupoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.DashboardResumoOfertaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.IntermediacaoLaboralDashboardResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.constants.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.IntermediacaoLaboralDashboardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class IntermediacaoLaboralDashboardServiceImpl implements IntermediacaoLaboralDashboardService {

    private final EmpregoDominioService empregoDominioService;
    private final IntermediacaoLaboralDashboardRepository dashboardRepository;

    @Override
    @Transactional(readOnly = true)
    public IntermediacaoLaboralDashboardResponse buscarResumo(Integer entidadeId) {
        if (entidadeId == null || entidadeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar a entidade selecionada.");
        }
        return new IntermediacaoLaboralDashboardResponse(
                dashboardRepository.contarOfertas(entidadeId),
                dashboardRepository.contarCandidaturas(entidadeId),
                dashboardRepository.calcularMediaVagasPorOferta(entidadeId),
                dashboardRepository.contarEstagiariosSelecionados(entidadeId),
                dashboardRepository.contarEstagiariosAvaliados(entidadeId),
                enriquecerGrupos(
                        dashboardRepository.listarOfertasPorEstado(entidadeId),
                        EmpregoDominio.DOMINIO_ESTADO_OFERTA
                ),
                enriquecerGrupos(
                        dashboardRepository.listarOfertasPorTipo(entidadeId),
                        EmpregoDominio.DOMINIO_TIPO_OFERTA
                ),
                enriquecerGrupos(
                        dashboardRepository.listarCandidaturasPorEstado(entidadeId),
                        EmpregoDominio.DOMINIO_STATUS_CANDIDATURA
                ),
                enriquecerResumoOfertas(dashboardRepository.listarResumoOfertas(entidadeId))
        );
    }

    private List<DashboardGrupoResponse> enriquecerGrupos(List<DashboardGrupoResponse> grupos, String dominio) {
        return grupos.stream()
                .map(item -> new DashboardGrupoResponse(
                        valorOficial(dominio, item.valor()),
                        empregoDominioService.descricao(dominio, item.valor()),
                        item.total()
                ))
                .toList();
    }

    private List<DashboardResumoOfertaResponse> enriquecerResumoOfertas(List<DashboardResumoOfertaResponse> ofertas) {
        return ofertas.stream()
                .map(item -> new DashboardResumoOfertaResponse(
                        item.idOferta(),
                        item.oferta(),
                        valorOficial(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipo()),
                        empregoDominioService.descricao(EmpregoDominio.DOMINIO_TIPO_OFERTA, item.tipo()),
                        item.totalVagas(),
                        item.totalCandidaturas(),
                        item.totalCandidaturasAprovadas(),
                        item.totalEstagiariosAvaliados()
                ))
                .toList();
    }

    private String valorOficial(String dominio, String valor) {
        return empregoDominioService.valorOficial(dominio, valor).orElse(valor);
    }
}
