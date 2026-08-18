package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CoordenadorOrientadorFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CoordenadorOrientadorListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CoordenadorOrientadorRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CoordenadorOrientadorRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CoordenadorOrientadorResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.PessoaGlobalResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.CoordenadorOrientadorService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/coordenadores-orientadores")
public class CoordenadorOrientadorController {

    private final CoordenadorOrientadorService coordenadorOrientadorService;

    @GetMapping
    public EmpregoApiResponse<List<CoordenadorOrientadorListaResponse>> listar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return EmpregoApiResponse.sucesso(
                "Coordenadores e orientadores listados com sucesso.",
                coordenadorOrientadorService.listar(new CoordenadorOrientadorFiltro(
                        nome,
                        tipo,
                        estado,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("pessoa")
    public EmpregoApiResponse<PessoaGlobalResponse> buscarPessoa(
            @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
            @RequestParam("numeroDocumento") String numeroDocumento
    ) {
        return EmpregoApiResponse.sucesso(
                "Pessoa encontrada com sucesso.",
                coordenadorOrientadorService.buscarPessoa(tipoDocumento, numeroDocumento)
        );
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<CoordenadorOrientadorResponse> buscarPorId(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Coordenador/orientador encontrado com sucesso.",
                coordenadorOrientadorService.buscarPorId(id)
        );
    }

    @GetMapping("{id}/ofertas")
    public EmpregoApiResponse<List<VagaListaResponse>> listarOfertasAssociadas(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Ofertas associadas listadas com sucesso.",
                coordenadorOrientadorService.listarOfertasAssociadas(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<CoordenadorOrientadorResponse> criar(@RequestBody CoordenadorOrientadorRequest request) {
        return EmpregoApiResponse.sucesso(
                "Coordenador/orientador criado com sucesso.",
                coordenadorOrientadorService.criar(request)
        );
    }

    @PutMapping("{id}")
    public EmpregoApiResponse<CoordenadorOrientadorResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody CoordenadorOrientadorRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Coordenador/orientador atualizado com sucesso.",
                coordenadorOrientadorService.atualizar(id, request)
        );
    }

    @PatchMapping("{id}/remover")
    public EmpregoApiResponse<CoordenadorOrientadorResponse> remover(
            @PathVariable Integer id,
            @RequestBody CoordenadorOrientadorRemoverRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Coordenador/orientador inativado com sucesso.",
                coordenadorOrientadorService.remover(id, request)
        );
    }
}
