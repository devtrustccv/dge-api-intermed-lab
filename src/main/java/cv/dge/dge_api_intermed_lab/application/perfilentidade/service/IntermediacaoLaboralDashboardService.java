package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.dashboard.IntermediacaoLaboralDashboardResponse;

public interface IntermediacaoLaboralDashboardService {

    IntermediacaoLaboralDashboardResponse buscarResumo(Integer entidadeId);
}
