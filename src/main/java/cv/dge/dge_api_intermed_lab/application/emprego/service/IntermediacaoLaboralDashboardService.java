package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.IntermediacaoLaboralDashboardResponse;

public interface IntermediacaoLaboralDashboardService {

    IntermediacaoLaboralDashboardResponse buscarResumo(Integer entidadeId);
}
