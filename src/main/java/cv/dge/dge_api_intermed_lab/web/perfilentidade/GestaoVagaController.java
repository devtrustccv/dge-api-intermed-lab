package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaColaboradorSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaDuplicacaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaEstadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVagaService;
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
@RequestMapping("/v1/vagas")
public class GestaoVagaController {

    private final GestaoVagaService gestaoVagaService;

    @GetMapping("opcoes/colaboradores")
    public EmpregoApiResponse<List<VagaColaboradorSelectResponse>> listarColaboradores(
            @RequestParam("tipo") String tipo
    ) {
        return EmpregoApiResponse.sucesso(
                "Colaboradores listados com sucesso.",
                gestaoVagaService.listarColaboradores(tipo)
        );
    }

    @GetMapping("opcoes/orientadores")
    public EmpregoApiResponse<List<VagaColaboradorSelectResponse>> listarOrientadores() {
        return EmpregoApiResponse.sucesso(
                "Orientadores listados com sucesso.",
                gestaoVagaService.listarOrientadores()
        );
    }

    @GetMapping("opcoes/coordenadores")
    public EmpregoApiResponse<List<VagaColaboradorSelectResponse>> listarCoordenadores() {
        return EmpregoApiResponse.sucesso(
                "Coordenadores listados com sucesso.",
                gestaoVagaService.listarCoordenadores()
        );
    }

    @GetMapping
    public EmpregoApiResponse<List<VagaListaResponse>> listar(
            @RequestParam(value = "tipoOferta", required = false) String tipoOferta,
            @RequestParam(value = "entidadeId", required = false) Integer entidadeId,
            @RequestParam(value = "ilha", required = false) String ilha,
            @RequestParam(value = "concelho", required = false) String concelho,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "codigoReferencia", required = false) String codigoReferencia,
            @RequestParam(value = "orientadorId", required = false) Integer orientadorId,
            @RequestParam(value = "coordenadorId", required = false) Integer coordenadorId,
            @RequestParam(value = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "pesquisa", required = false) String pesquisa
    ) {
        return EmpregoApiResponse.sucesso(
                "Vagas listadas com sucesso.",
                gestaoVagaService.listar(new VagaFiltro(
                        tipoOferta,
                        entidadeId,
                        ilha,
                        concelho,
                        estado,
                        codigoReferencia,
                        orientadorId,
                        coordenadorId,
                        dataInicio,
                        dataFim,
                        pesquisa
                ))
        );
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<VagaResponse> buscarPorId(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Vaga encontrada com sucesso.",
                gestaoVagaService.buscarPorId(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<VagaResponse> criar(@RequestBody VagaRequest request) {
        return EmpregoApiResponse.sucesso(
                "Vaga criada com sucesso.",
                gestaoVagaService.criar(request)
        );
    }

    @PostMapping("rascunho")
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<VagaResponse> criarRascunho(@RequestBody VagaRequest request) {
        return EmpregoApiResponse.sucesso(
                "Rascunho da vaga criado com sucesso.",
                gestaoVagaService.criarRascunho(request)
        );
    }

    @PutMapping("{id}")
    public EmpregoApiResponse<VagaResponse> atualizar(@PathVariable Integer id, @RequestBody VagaRequest request) {
        return EmpregoApiResponse.sucesso(
                "Vaga atualizada com sucesso.",
                gestaoVagaService.atualizar(id, request)
        );
    }

    @PatchMapping("{id}/estado")
    public EmpregoApiResponse<VagaResponse> alterarEstado(@PathVariable Integer id, @RequestBody VagaEstadoRequest request) {
        return EmpregoApiResponse.sucesso(
                "Estado da vaga atualizado com sucesso.",
                gestaoVagaService.alterarEstado(id, request)
        );
    }

    @PatchMapping("{id}/validar")
    public EmpregoApiResponse<VagaResponse> validar(@PathVariable Integer id, @RequestBody VagaValidacaoRequest request) {
        return EmpregoApiResponse.sucesso(
                "Vaga validada com sucesso.",
                gestaoVagaService.validar(id, request)
        );
    }

    @GetMapping("{id}/duplicar")
    public EmpregoApiResponse<VagaDuplicacaoResponse> prepararDuplicacao(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Dados da vaga preparados para duplicacao com sucesso.",
                gestaoVagaService.prepararDuplicacao(id)
        );
    }
}
