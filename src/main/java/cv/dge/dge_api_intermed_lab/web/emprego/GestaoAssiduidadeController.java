package cv.dge.dge_api_intermed_lab.web.emprego;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EmpregoDominioOpcaoResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.service.GestaoAssiduidadeService;
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

    @GetMapping("opcoes/tipos")
    public EmpregoApiResponse<List<EmpregoDominioOpcaoResponse>> listarTiposAssiduidade() {
        return EmpregoApiResponse.sucesso(
                "Tipos de assiduidade listados com sucesso.",
                gestaoAssiduidadeService.listarTiposAssiduidade()
        );
    }

    @GetMapping("opcoes/estados")
    public EmpregoApiResponse<List<EmpregoDominioOpcaoResponse>> listarEstadosAssiduidade() {
        return EmpregoApiResponse.sucesso(
                "Estados de assiduidade listados com sucesso.",
                gestaoAssiduidadeService.listarEstadosAssiduidade()
        );
    }

    @GetMapping("opcoes/decisoes")
    public EmpregoApiResponse<List<EmpregoDominioOpcaoResponse>> listarDecisoesAssiduidade() {
        return EmpregoApiResponse.sucesso(
                "Decisoes de assiduidade listadas com sucesso.",
                gestaoAssiduidadeService.listarDecisoesAssiduidade()
        );
    }
}
