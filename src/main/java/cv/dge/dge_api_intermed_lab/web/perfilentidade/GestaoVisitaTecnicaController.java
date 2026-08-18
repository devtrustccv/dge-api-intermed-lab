package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaAtualizacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCefpSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaExecutadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaObservacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaValidacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVisitaTecnicaService;
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
@RequestMapping("/v1/visitas-tecnicas")
public class GestaoVisitaTecnicaController {

    private final GestaoVisitaTecnicaService gestaoVisitaTecnicaService;

    @GetMapping
    public EmpregoApiResponse<List<VisitaTecnicaListaResponse>> listar(
            @RequestParam(value = "entidadeId", required = false) Integer entidadeId,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "agendadoPor", required = false) String agendadoPor,
            @RequestParam(value = "cefpId", required = false) Integer cefpId,
            @RequestParam(value = "dataVisita", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVisita,
            @RequestParam(value = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return EmpregoApiResponse.sucesso(
                "Visitas tecnicas listadas com sucesso.",
                gestaoVisitaTecnicaService.listar(new VisitaTecnicaFiltro(
                        entidadeId,
                        estado,
                        agendadoPor,
                        cefpId,
                        dataVisita,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<VisitaTecnicaDetalheResponse> buscarPorId(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Visita tecnica encontrada com sucesso.",
                gestaoVisitaTecnicaService.buscarPorId(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<VisitaTecnicaDetalheResponse> criar(@RequestBody VisitaTecnicaRequest request) {
        return EmpregoApiResponse.sucesso(
                "Visita tecnica criada com sucesso.",
                gestaoVisitaTecnicaService.criar(request)
        );
    }

    @PutMapping("{id}")
    public EmpregoApiResponse<VisitaTecnicaDetalheResponse> atualizar(
            @PathVariable Integer id,
            @RequestBody VisitaTecnicaAtualizacaoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Visita tecnica atualizada com sucesso.",
                gestaoVisitaTecnicaService.atualizar(id, request)
        );
    }

    @PatchMapping("{id}/validacao")
    public EmpregoApiResponse<VisitaTecnicaDetalheResponse> validar(
            @PathVariable Integer id,
            @RequestBody VisitaTecnicaValidacaoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Agendamento de visita tecnica validado com sucesso.",
                gestaoVisitaTecnicaService.validar(id, request)
        );
    }

    @PatchMapping("{id}/executado")
    public EmpregoApiResponse<VisitaTecnicaDetalheResponse> marcarComoExecutado(
            @PathVariable Integer id,
            @RequestBody VisitaTecnicaExecutadoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Visita tecnica marcada como executada com sucesso.",
                gestaoVisitaTecnicaService.marcarComoExecutado(id, request)
        );
    }

    @PatchMapping("{id}/observacoes")
    public EmpregoApiResponse<VisitaTecnicaDetalheResponse> registarObservacoes(
            @PathVariable Integer id,
            @RequestBody VisitaTecnicaObservacaoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Observacoes da visita tecnica registadas com sucesso.",
                gestaoVisitaTecnicaService.registarObservacoes(id, request)
        );
    }

    @GetMapping("opcoes/candidatos")
    public EmpregoApiResponse<List<VisitaTecnicaCandidatoSelectResponse>> listarCandidatos(
            @RequestParam("entidadeId") Integer entidadeId
    ) {
        return EmpregoApiResponse.sucesso(
                "Candidatos listados com sucesso.",
                gestaoVisitaTecnicaService.listarCandidatos(entidadeId)
        );
    }

    @GetMapping("opcoes/cefps")
    public EmpregoApiResponse<List<VisitaTecnicaCefpSelectResponse>> listarCefps() {
        return EmpregoApiResponse.sucesso(
                "CEFPs listados com sucesso.",
                gestaoVisitaTecnicaService.listarCefps()
        );
    }
}
