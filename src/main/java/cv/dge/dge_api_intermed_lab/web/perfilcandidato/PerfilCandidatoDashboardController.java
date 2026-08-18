package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoDashboardResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.PerfilCandidatoDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/dashboard")
public class PerfilCandidatoDashboardController {

    private final PerfilCandidatoDashboardService dashboardService;

    @GetMapping
    public PerfilCandidatoApiResponse<PerfilCandidatoDashboardResponse> buscarDashboard(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) Integer ano
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Dashboard do candidato carregado com sucesso.",
                dashboardService.buscarDashboard(pessoaId, ano)
        );
    }
}
