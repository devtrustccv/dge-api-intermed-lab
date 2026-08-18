package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.*;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoAvaliacaoEstagiarioService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/avaliacoes-estagiarios")
public class GestaoAvaliacaoEstagiarioController {
    private final GestaoAvaliacaoEstagiarioService service;

    @GetMapping
    public EmpregoApiResponse<List<AvaliacaoEstagiarioListaResponse>> listar(
            @RequestParam Integer entidadeId, @RequestParam(required=false) Long pessoaId,
            @RequestParam(required=false) String tipoAvaliacao, @RequestParam(required=false) String periodoReferencia,
            @RequestParam(required=false) LocalDate dataInicio, @RequestParam(required=false) LocalDate dataFim) {
        return EmpregoApiResponse.sucesso("Avaliacoes de estagiarios listadas com sucesso.",
                service.listar(new AvaliacaoEstagiarioFiltro(entidadeId, pessoaId, tipoAvaliacao,
                        periodoReferencia, dataInicio, dataFim)));
    }

    @GetMapping("/{id}")
    public EmpregoApiResponse<AvaliacaoEstagiarioDetalheResponse> buscar(@PathVariable Integer id,
            @RequestParam Integer entidadeId) {
        return EmpregoApiResponse.sucesso("Avaliacao de estagiario encontrada com sucesso.", service.buscarPorId(id, entidadeId));
    }

    @PostMapping
    public EmpregoApiResponse<AvaliacaoEstagiarioDetalheResponse> criar(@RequestParam Integer entidadeId,
            @RequestBody AvaliacaoEstagiarioRequest request) {
        return EmpregoApiResponse.sucesso("Avaliacao de estagiario criada com sucesso.", service.criar(entidadeId, request));
    }

    @PutMapping("/{id}")
    public EmpregoApiResponse<AvaliacaoEstagiarioDetalheResponse> atualizar(@PathVariable Integer id,
            @RequestParam Integer entidadeId, @RequestBody AvaliacaoEstagiarioRequest request) {
        return EmpregoApiResponse.sucesso("Avaliacao de estagiario atualizada com sucesso.", service.atualizar(id, entidadeId, request));
    }

    @GetMapping("/opcoes/estagiarios")
    public EmpregoApiResponse<List<AvaliacaoEstagiarioSelectResponse>> listarEstagiarios(@RequestParam Integer entidadeId) {
        return EmpregoApiResponse.sucesso("Estagiarios listados com sucesso.", service.listarEstagiarios(entidadeId));
    }
}
