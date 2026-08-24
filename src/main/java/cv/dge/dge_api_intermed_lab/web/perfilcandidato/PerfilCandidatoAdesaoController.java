package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.PerfilCandidatoAdesaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/adesao")
public class PerfilCandidatoAdesaoController {

    private final PerfilCandidatoAdesaoService adesaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<AdesaoJovemResponse> confirmarAdesao(
            @RequestParam(required = false) Long pessoaId,
            @RequestBody AdesaoJovemRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "A sua adesão foi submetida com sucesso.",
                adesaoService.confirmarAdesao(pessoaId, request)
        );
    }
}
