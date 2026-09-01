package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteAcaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.ServicoCandidatoService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/prestacoes-servicos/candidato")
public class PerfilCandidatoServicoCandidatoController {

    private final ServicoCandidatoService servicoService;

    @GetMapping
    public PerfilCandidatoApiResponse<List<ServicoCandidatoListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String tipoServico,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Serviços indicados ao candidato carregados com sucesso.",
                servicoService.listar(new ServicoCandidatoFiltro(
                        pessoaId,
                        tipoServico,
                        estado,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("opcoes")
    public PerfilCandidatoApiResponse<ServicoCandidatoOpcoesResponse> listarOpcoes() {
        return PerfilCandidatoApiResponse.sucesso(
                "Opções da prestação de serviços carregadas com sucesso.",
                servicoService.listarOpcoes()
        );
    }

    @GetMapping("{servicoId}")
    public PerfilCandidatoApiResponse<ServicoCandidatoDetalheResponse> buscarPorId(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes do serviço carregados com sucesso.",
                servicoService.buscarPorId(servicoId, pessoaId)
        );
    }

    @PatchMapping("{servicoId}/aceitar")
    public PerfilCandidatoApiResponse<ServicoCandidatoDetalheResponse> aceitar(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteAcaoRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Indicação aceite com sucesso.",
                servicoService.aceitar(servicoId, pessoaId, utilizador(request))
        );
    }

    @PatchMapping("{servicoId}/recusar")
    public PerfilCandidatoApiResponse<ServicoCandidatoDetalheResponse> recusar(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteAcaoRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Indicação recusada com sucesso.",
                servicoService.recusar(servicoId, pessoaId, utilizador(request))
        );
    }

    private String utilizador(ServicoContratanteAcaoRequest request) {
        return request == null ? null : request.utilizador();
    }
}
