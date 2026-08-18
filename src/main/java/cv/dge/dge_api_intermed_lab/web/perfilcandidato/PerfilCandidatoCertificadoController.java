package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CertificadoEstagioEmissaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CertificadoEstagioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.PerfilCandidatoCertificadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/certificados/estagio")
public class PerfilCandidatoCertificadoController {

    private final PerfilCandidatoCertificadoService certificadoService;

    @GetMapping
    public PerfilCandidatoApiResponse<CertificadoEstagioResponse> consultar(
            @RequestParam(required = false) Integer colocacaoId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Dados do certificado carregados com sucesso.",
                certificadoService.consultar(colocacaoId, pessoaId)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<CertificadoEstagioResponse> emitir(
            @RequestParam(required = false) Integer colocacaoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody CertificadoEstagioEmissaoRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Certificado emitido com sucesso.",
                certificadoService.emitir(colocacaoId, pessoaId, request)
        );
    }

    @GetMapping("/validar/{codigoContraprova}")
    public PerfilCandidatoApiResponse<CertificadoEstagioResponse> validar(
            @PathVariable String codigoContraprova
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "O certificado é válido.",
                certificadoService.validar(codigoContraprova)
        );
    }
}

