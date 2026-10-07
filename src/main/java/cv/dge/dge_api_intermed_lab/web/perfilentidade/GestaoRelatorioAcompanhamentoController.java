package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoRelatorioAcompanhamentoService;
import cv.dge.dge_api_intermed_lab.web.ApiErrorMessageResolver;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/relatorios-acompanhamento")
public class GestaoRelatorioAcompanhamentoController {

    private final GestaoRelatorioAcompanhamentoService service;
    private final ObjectMapper objectMapper;

    @GetMapping
    public EmpregoApiResponse<List<RelatorioAcompanhamentoListaResponse>> listar(
            @RequestParam Integer entidadeId,
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String estagiario,
            @RequestParam(required = false) String codigoReferencia,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorios de acompanhamento listados com sucesso.",
                service.listar(new RelatorioAcompanhamentoFiltro(
                        entidadeId, pessoaId, estagiario, codigoReferencia, dataInicio, dataFim)));
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> buscarPorId(
            @PathVariable Integer id,
            @RequestParam Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento encontrado com sucesso.",
                service.buscarPorId(id, entidadeId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> criar(
            @RequestParam Integer entidadeId,
            @RequestBody RelatorioAcompanhamentoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento criado com sucesso.",
                service.criar(entidadeId, request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> criarMultipart(
            @RequestParam Integer entidadeId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "relatorioAnexo", required = false) MultipartFile relatorioAnexo,
            @RequestPart(value = "ficheiro", required = false) MultipartFile ficheiro,
            @RequestPart(value = "documento", required = false) MultipartFile documento
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento criado com sucesso.",
                service.criar(
                        entidadeId,
                        converterDados(dadosJson),
                        primeiroComConteudo(relatorioAnexo, ficheiro, documento)
                ));
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> atualizar(
            @PathVariable Integer id,
            @RequestParam Integer entidadeId,
            @RequestBody RelatorioAcompanhamentoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento atualizado com sucesso.",
                service.atualizar(id, entidadeId, request));
    }

    @PutMapping(value = "{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> atualizarMultipart(
            @PathVariable Integer id,
            @RequestParam Integer entidadeId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "relatorioAnexo", required = false) MultipartFile relatorioAnexo,
            @RequestPart(value = "ficheiro", required = false) MultipartFile ficheiro,
            @RequestPart(value = "documento", required = false) MultipartFile documento
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento atualizado com sucesso.",
                service.atualizar(
                        id,
                        entidadeId,
                        converterDados(dadosJson),
                        primeiroComConteudo(relatorioAnexo, ficheiro, documento)
                ));
    }

    @PatchMapping("{id}/remover")
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> remover(
            @PathVariable Integer id,
            @RequestParam Integer entidadeId,
            @RequestBody RelatorioAcompanhamentoRemoverRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento removido com sucesso.",
                service.remover(id, entidadeId, request));
    }

    @GetMapping("opcoes")
    public EmpregoApiResponse<List<RelatorioAcompanhamentoOfertaSelectResponse>> listarOpcoes(
            @RequestParam Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Ofertas de estágio e estagiários associados listados com sucesso.",
                service.listarOpcoes(entidadeId));
    }

    private RelatorioAcompanhamentoRequest converterDados(String dadosJson) {
        if (dadosJson == null || dadosJson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha os dados do relatório de acompanhamento antes de guardar."
            );
        }
        try {
            return objectMapper.readValue(dadosJson, RelatorioAcompanhamentoRequest.class);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ApiErrorMessageResolver.corpoInvalido(ex, "dados do relatório de acompanhamento"),
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
