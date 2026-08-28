package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaColocacaoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/colocacoes")
public class PerfilCandidatoColocacaoController {

    private final MinhaColocacaoService colocacaoService;

    @GetMapping
    public PerfilCandidatoApiResponse<List<MinhaColocacaoListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Colocações carregadas com sucesso.",
                colocacaoService.listar(pessoaId)
        );
    }

    @GetMapping("{colocacaoId}")
    public PerfilCandidatoApiResponse<MinhaColocacaoDetalheResponse> buscarPorId(
            @PathVariable Integer colocacaoId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes da colocação carregados com sucesso.",
                colocacaoService.buscarPorId(colocacaoId, pessoaId)
        );
    }
}
