package cv.dge.dge_api_intermed_lab.web.emprego;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.IntermediacaoLaboralDashboardResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.service.IntermediacaoLaboralDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/intermediacao-laboral/dashboard")
public class IntermediacaoLaboralDashboardController {

    private final IntermediacaoLaboralDashboardService dashboardService;

    @GetMapping
    public IntermediacaoLaboralDashboardResponse buscarResumo(
            @RequestParam(value = "entidadeId", required = false) Integer entidadeId
    ) {
        return dashboardService.buscarResumo(entidadeId);
    }
}
