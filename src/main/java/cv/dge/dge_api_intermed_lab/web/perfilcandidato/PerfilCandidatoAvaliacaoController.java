package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaAvaliacaoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/avaliacoes")
public class PerfilCandidatoAvaliacaoController {

    private final MinhaAvaliacaoService avaliacaoService;

    @GetMapping
    public PerfilCandidatoApiResponse<List<MinhaAvaliacaoListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Avaliações carregadas com sucesso.",
                avaliacaoService.listar(pessoaId)
        );
    }

    @GetMapping("{avaliacaoId}")
    public PerfilCandidatoApiResponse<MinhaAvaliacaoDetalheResponse> buscarPorId(
            @PathVariable Integer avaliacaoId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes da avaliação carregados com sucesso.",
                avaliacaoService.buscarPorId(avaliacaoId, pessoaId)
        );
    }
}
