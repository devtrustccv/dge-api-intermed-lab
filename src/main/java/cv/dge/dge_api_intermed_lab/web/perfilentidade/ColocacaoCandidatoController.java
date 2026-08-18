package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.ColocacaoCandidatoService;
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
@RequestMapping("/v1/colocacoes-candidatos")
public class ColocacaoCandidatoController {

    private final ColocacaoCandidatoService colocacaoCandidatoService;

    @GetMapping
    public EmpregoApiResponse<List<ColocacaoCandidatoListaResponse>> listar(
            @RequestParam(value = "tipoOferta", required = false) String tipoOferta,
            @RequestParam(value = "codigoReferencia", required = false) String codigoReferencia,
            @RequestParam(value = "pessoaId", required = false) Long pessoaId,
            @RequestParam(value = "tipoContrato", required = false) String tipoContrato,
            @RequestParam(value = "dataInicioPrevisto", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicioPrevisto,
            @RequestParam(value = "dataRegistoInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRegistoInicio,
            @RequestParam(value = "dataRegistoFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRegistoFim,
            @RequestParam(value = "entidadeId", required = false) Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacoes listadas com sucesso.",
                colocacaoCandidatoService.listar(new ColocacaoCandidatoFiltro(
                        tipoOferta,
                        codigoReferencia,
                        pessoaId,
                        tipoContrato,
                        dataInicioPrevisto,
                        dataRegistoInicio,
                        dataRegistoFim,
                        entidadeId
                ))
        );
    }

    @GetMapping("ofertas")
    public EmpregoApiResponse<List<ColocacaoOfertaSelectResponse>> listarOfertasPorTipoEEntidade(
            @RequestParam("tipoOferta") String tipoOferta,
            @RequestParam("entidadeId") Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Ofertas listadas com sucesso.",
                colocacaoCandidatoService.listarOfertasPorTipoEEntidade(tipoOferta, entidadeId)
        );
    }

    @GetMapping("candidatos")
    public EmpregoApiResponse<List<ColocacaoCandidatoSelectResponse>> listarCandidatosPorOferta(
            @RequestParam("ofertaId") Integer ofertaId
    ) {
        return EmpregoApiResponse.sucesso(
                "Candidatos da oferta listados com sucesso.",
                colocacaoCandidatoService.listarCandidatosPorOferta(ofertaId)
        );
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<ColocacaoCandidatoResponse> buscarPorId(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Colocacao encontrada com sucesso.",
                colocacaoCandidatoService.buscarPorId(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<ColocacaoCandidatoResponse> criar(@RequestBody ColocacaoCandidatoRequest request) {
        return EmpregoApiResponse.sucesso(
                "Colocacao criada com sucesso.",
                colocacaoCandidatoService.criar(request)
        );
    }

    @PutMapping("{id}")
    public EmpregoApiResponse<ColocacaoCandidatoResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody ColocacaoCandidatoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao atualizada com sucesso.",
                colocacaoCandidatoService.atualizar(id, request)
        );
    }

    @PatchMapping("{id}/remover")
    public EmpregoApiResponse<ColocacaoCandidatoResponse> remover(
            @PathVariable Integer id,
            @RequestBody ColocacaoCandidatoRemoverRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao removida com sucesso.",
                colocacaoCandidatoService.remover(id, request)
        );
    }
}
