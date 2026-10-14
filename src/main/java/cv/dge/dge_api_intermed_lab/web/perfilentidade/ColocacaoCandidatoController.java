package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.comum.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.ColocacaoCandidatoService;
import cv.dge.dge_api_intermed_lab.web.ApiErrorMessageResolver;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/colocacoes-candidatos")
public class ColocacaoCandidatoController {

    private final ColocacaoCandidatoService colocacaoCandidatoService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public EmpregoApiResponse<List<ColocacaoCandidatoListaResponse>> listar(
            @RequestParam(value = "tipoOferta", required = false) String tipoOferta,
            @RequestParam(value = "codigoReferencia", required = false) String codigoReferencia,
            @RequestParam(value = "pessoaId", required = false) Long pessoaId,
            @RequestParam(value = "candidato", required = false) String candidato,
            @RequestParam(value = "tipoContrato", required = false) String tipoContrato,
            @RequestParam(value = "dataInicioPrevisto", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicioPrevisto,
            @RequestParam(value = "dataRegistoInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRegistoInicio,
            @RequestParam(value = "dataRegistoFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRegistoFim,
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestParam(value = "search", required = false) String search
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacoes listadas com sucesso.",
                colocacaoCandidatoService.listar(new ColocacaoCandidatoFiltro(
                        tipoOferta,
                        codigoReferencia,
                        pessoaId,
                        candidato,
                        tipoContrato,
                        dataInicioPrevisto,
                        dataRegistoInicio,
                        dataRegistoFim,
                        entidadeId,
                        search
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
    public EmpregoApiResponse<List<ColocacaoCandidatoSelectResponse>> listarCandidatos(
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestParam(value = "ofertaId", required = false) Integer ofertaId
    ) {
        return EmpregoApiResponse.sucesso(
                "Candidatos da entidade listados com sucesso.",
                colocacaoCandidatoService.listarCandidatos(entidadeId, ofertaId)
        );
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<ColocacaoCandidatoResponse> buscarPorId(
            @PathVariable Integer id,
            @RequestParam("entidadeId") Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao encontrada com sucesso.",
                colocacaoCandidatoService.buscarPorId(id, entidadeId)
        );
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<ColocacaoCandidatoResponse> criar(
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestBody ColocacaoCandidatoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao criada com sucesso.",
                colocacaoCandidatoService.criar(entidadeId, request)
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<ColocacaoCandidatoResponse> criarComContrato(
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "contrato", required = false) MultipartFile contrato,
            @RequestPart(value = "contratoAnexo", required = false) MultipartFile contratoAnexo,
            @RequestPart(value = "ficheiro", required = false) MultipartFile ficheiro,
            @RequestPart(value = "documento", required = false) MultipartFile documento
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao criada com sucesso.",
                colocacaoCandidatoService.criar(
                        entidadeId,
                        converterDados(dadosJson),
                        primeiroComConteudo(contrato, contratoAnexo, ficheiro, documento)
                )
        );
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmpregoApiResponse<ColocacaoCandidatoResponse> atualizar(
            @PathVariable Integer id,
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestBody ColocacaoCandidatoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao atualizada com sucesso.",
                colocacaoCandidatoService.atualizar(id, entidadeId, request)
        );
    }

    @PutMapping(value = "{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmpregoApiResponse<ColocacaoCandidatoResponse> atualizarComContrato(
            @PathVariable Integer id,
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "contrato", required = false) MultipartFile contrato,
            @RequestPart(value = "contratoAnexo", required = false) MultipartFile contratoAnexo,
            @RequestPart(value = "ficheiro", required = false) MultipartFile ficheiro,
            @RequestPart(value = "documento", required = false) MultipartFile documento
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao atualizada com sucesso.",
                colocacaoCandidatoService.atualizar(
                        id,
                        entidadeId,
                        converterDados(dadosJson),
                        primeiroComConteudo(contrato, contratoAnexo, ficheiro, documento)
                )
        );
    }

    @PatchMapping("{id}/remover")
    public EmpregoApiResponse<ColocacaoCandidatoResponse> remover(
            @PathVariable Integer id,
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestBody ColocacaoCandidatoRemoverRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Colocacao removida com sucesso.",
                colocacaoCandidatoService.remover(id, entidadeId, request)
        );
    }

    private ColocacaoCandidatoRequest converterDados(String dadosJson) {
        if (dadosJson == null || dadosJson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha os dados da colocação antes de gravar."
            );
        }
        try {
            return objectMapper.readValue(dadosJson, ColocacaoCandidatoRequest.class);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ApiErrorMessageResolver.corpoInvalido(ex, "dados da colocação"),
                    ex
            );
        }
    }

    private MultipartFile primeiroComConteudo(MultipartFile... ficheiros) {
        for (MultipartFile ficheiro : ficheiros) {
            if (ficheiro != null && !ficheiro.isEmpty()) {
                return ficheiro;
            }
        }
        return null;
    }
}
