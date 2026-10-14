package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.entrevista.MinhaEntrevistaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.comum.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaEntrevistaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/entrevistas")
public class PerfilCandidatoEntrevistaController {

    private final MinhaEntrevistaService entrevistaService;

    @GetMapping
    public PerfilCandidatoApiResponse<List<MinhaEntrevistaListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(value = "search", required = false) String search
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Entrevistas carregadas com sucesso.",
                entrevistaService.listar(pessoaId, search)
        );
    }
}
