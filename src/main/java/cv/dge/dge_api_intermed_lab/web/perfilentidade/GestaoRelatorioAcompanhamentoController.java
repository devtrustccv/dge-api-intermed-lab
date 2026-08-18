package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoRelatorioAcompanhamentoService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/relatorios-acompanhamento")
public class GestaoRelatorioAcompanhamentoController {

    private final GestaoRelatorioAcompanhamentoService service;

    @GetMapping
    public EmpregoApiResponse<List<RelatorioAcompanhamentoListaResponse>> listar(
            @RequestParam(required = false) Integer entidadeId,
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String codigoReferencia,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorios de acompanhamento listados com sucesso.",
                service.listar(new RelatorioAcompanhamentoFiltro(
                        entidadeId, pessoaId, codigoReferencia, dataInicio, dataFim)));
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> buscarPorId(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento encontrado com sucesso.",
                service.buscarPorId(id, entidadeId));
    }

    @PostMapping
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> criar(
            @RequestParam(required = false) Integer entidadeId,
            @RequestBody RelatorioAcompanhamentoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento criado com sucesso.",
                service.criar(entidadeId, request));
    }

    @PutMapping("{id}")
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> atualizar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer entidadeId,
            @RequestBody RelatorioAcompanhamentoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento atualizado com sucesso.",
                service.atualizar(id, entidadeId, request));
    }

    @PatchMapping("{id}/remover")
    public EmpregoApiResponse<RelatorioAcompanhamentoDetalheResponse> remover(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer entidadeId,
            @RequestBody RelatorioAcompanhamentoRemoverRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Relatorio de acompanhamento removido com sucesso.",
                service.remover(id, entidadeId, request));
    }

    @GetMapping("opcoes")
    public EmpregoApiResponse<List<RelatorioAcompanhamentoOfertaSelectResponse>> listarOpcoes(
            @RequestParam(required = false) Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Ofertas de estágio e estagiários associados listados com sucesso.",
                service.listarOpcoes(entidadeId));
    }
}
