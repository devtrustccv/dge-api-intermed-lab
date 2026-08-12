package cv.dge.dge_api_intermed_lab.web.emprego;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.service.GestaoAcompanhamentoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/acompanhamentos")
public class GestaoAcompanhamentoController {

    private final GestaoAcompanhamentoService gestaoAcompanhamentoService;

    @GetMapping("estagiarios-selecionados")
    public EmpregoApiResponse<List<AcompanhamentoEstagiarioListaResponse>> listarEstagiariosSelecionados(
            @RequestParam(value = "estagiarioId", required = false) Long estagiarioId,
            @RequestParam(value = "ofertaId", required = false) Integer ofertaId
    ) {
        return EmpregoApiResponse.sucesso(
                "Estagiarios selecionados listados com sucesso.",
                gestaoAcompanhamentoService.listarEstagiariosSelecionados(
                        new AcompanhamentoEstagiarioFiltro(estagiarioId, ofertaId)
                )
        );
    }

    @GetMapping("estagiarios-selecionados/opcoes/estagiarios")
    public EmpregoApiResponse<List<AcompanhamentoEstagiarioSelectResponse>> listarEstagiariosParaFiltro() {
        return EmpregoApiResponse.sucesso(
                "Estagiarios listados com sucesso.",
                gestaoAcompanhamentoService.listarEstagiariosSelecionadosParaFiltro()
        );
    }

    @GetMapping("estagiarios-selecionados/opcoes/ofertas")
    public EmpregoApiResponse<List<AcompanhamentoOfertaSelectResponse>> listarOfertasParaFiltro() {
        return EmpregoApiResponse.sucesso(
                "Ofertas listadas com sucesso.",
                gestaoAcompanhamentoService.listarOfertasComEstagiariosSelecionados()
        );
    }
}
