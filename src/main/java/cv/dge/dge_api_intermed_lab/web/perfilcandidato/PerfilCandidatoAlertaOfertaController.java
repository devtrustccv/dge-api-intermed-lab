package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.ConfiguracaoAlertaOfertaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/alertas-ofertas")
public class PerfilCandidatoAlertaOfertaController {

    private final ConfiguracaoAlertaOfertaService alertaService;

    @GetMapping
    public PerfilCandidatoApiResponse<List<AlertaOfertaListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Configurações de alerta carregadas com sucesso.",
                alertaService.listar(pessoaId)
        );
    }

    @GetMapping("opcoes")
    public PerfilCandidatoApiResponse<AlertaOfertaOpcoesResponse> listarOpcoes(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String ilha
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Opções da configuração carregadas com sucesso.",
                alertaService.listarOpcoes(pessoaId, ilha)
        );
    }

    @GetMapping("{alertaId}")
    public PerfilCandidatoApiResponse<AlertaOfertaDetalheResponse> buscarPorId(
            @PathVariable Integer alertaId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes da configuração carregados com sucesso.",
                alertaService.buscarPorId(alertaId, pessoaId)
        );
    }

    @PostMapping
    public PerfilCandidatoApiResponse<AlertaOfertaDetalheResponse> criar(
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) AlertaOfertaRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Configuração de alerta criada com sucesso.",
                alertaService.criar(pessoaId, request)
        );
    }

    @PutMapping("{alertaId}")
    public PerfilCandidatoApiResponse<AlertaOfertaDetalheResponse> atualizar(
            @PathVariable Integer alertaId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) AlertaOfertaRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Configuração de alerta atualizada com sucesso.",
                alertaService.atualizar(alertaId, pessoaId, request)
        );
    }
}
