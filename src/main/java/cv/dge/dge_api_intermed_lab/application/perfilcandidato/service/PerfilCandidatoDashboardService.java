package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoDashboardResponse;

public interface PerfilCandidatoDashboardService {

    PerfilCandidatoDashboardResponse buscarDashboard(Long pessoaId, Integer ano);
}
