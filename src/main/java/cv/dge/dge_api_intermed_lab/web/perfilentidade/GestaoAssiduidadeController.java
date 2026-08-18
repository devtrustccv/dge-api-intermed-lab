package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoAssiduidadeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assiduidades")
public class GestaoAssiduidadeController {

    private final GestaoAssiduidadeService gestaoAssiduidadeService;

    @GetMapping
    public EmpregoApiResponse<List<AssiduidadeEstagiarioListaResponse>> listar(
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestParam(value = "estagiarioId", required = false) Long estagiarioId,
            @RequestParam(value = "ofertaId", required = false) Integer ofertaId,
            @RequestParam(value = "tipoAssiduidade", required = false) String tipoAssiduidade,
            @RequestParam(value = "estado", required = false) String estado
    ) {
        return EmpregoApiResponse.sucesso(
                "Assiduidades listadas com sucesso.",
                gestaoAssiduidadeService.listar(new AssiduidadeEstagiarioFiltro(
                        entidadeId,
                        estagiarioId,
                        ofertaId,
                        tipoAssiduidade,
                        estado
                ))
        );
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<AssiduidadeEstagiarioDetalheResponse> buscarPorId(
            @PathVariable Integer id,
            @RequestParam("entidadeId") Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Assiduidade encontrada com sucesso.",
                gestaoAssiduidadeService.buscarPorId(id, entidadeId)
        );
    }

    @PatchMapping("{id}/validacao")
    public EmpregoApiResponse<AssiduidadeEstagiarioDetalheResponse> validar(
            @PathVariable Integer id,
            @RequestParam("entidadeId") Integer entidadeId,
            @RequestBody AssiduidadeValidacaoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Assiduidade validada com sucesso.",
                gestaoAssiduidadeService.validar(id, entidadeId, request)
        );
    }

    @GetMapping("opcoes/estagiarios")
    public EmpregoApiResponse<List<AssiduidadeEstagiarioSelectResponse>> listarEstagiariosParaFiltro(
            @RequestParam("entidadeId") Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Estagiarios listados com sucesso.",
                gestaoAssiduidadeService.listarEstagiariosParaFiltro(entidadeId)
        );
    }

    @GetMapping("opcoes/ofertas")
    public EmpregoApiResponse<List<AssiduidadeOfertaSelectResponse>> listarOfertasParaFiltro(
            @RequestParam("entidadeId") Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Ofertas listadas com sucesso.",
                gestaoAssiduidadeService.listarOfertasParaFiltro(entidadeId)
        );
    }
}
