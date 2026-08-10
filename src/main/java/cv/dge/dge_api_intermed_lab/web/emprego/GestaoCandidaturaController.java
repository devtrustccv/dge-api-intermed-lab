package cv.dge.dge_api_intermed_lab.web.emprego;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaAvaliacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EntrevistaAgendamentoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EntrevistaResultadoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.service.GestaoCandidaturaService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/candidaturas")
public class GestaoCandidaturaController {

    private final GestaoCandidaturaService gestaoCandidaturaService;

    @GetMapping
    public EmpregoApiResponse<List<CandidaturaListaResponse>> listar(
            @RequestParam(value = "candidatoId", required = false) Long candidatoId,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "tipoOferta", required = false) String tipoOferta,
            @RequestParam(value = "ofertaId", required = false) Integer ofertaId,
            @RequestParam(value = "canal", required = false) String canal,
            @RequestParam(value = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return EmpregoApiResponse.sucesso(
                "Candidaturas listadas com sucesso.",
                gestaoCandidaturaService.listar(new CandidaturaFiltro(
                        candidatoId,
                        estado,
                        tipoOferta,
                        ofertaId,
                        canal,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("{id}")
    public EmpregoApiResponse<CandidaturaDetalheResponse> buscarPorId(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Candidatura encontrada com sucesso.",
                gestaoCandidaturaService.buscarPorId(id)
        );
    }

    @PatchMapping("{id}/avaliacao")
    public EmpregoApiResponse<CandidaturaDetalheResponse> avaliar(
            @PathVariable Integer id,
            @RequestBody CandidaturaAvaliacaoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Candidatura avaliada com sucesso.",
                gestaoCandidaturaService.avaliar(id, request)
        );
    }

    @GetMapping("{id}/entrevistas")
    public EmpregoApiResponse<List<EntrevistaResponse>> listarEntrevistas(@PathVariable Integer id) {
        return EmpregoApiResponse.sucesso(
                "Entrevistas listadas com sucesso.",
                gestaoCandidaturaService.listarEntrevistas(id)
        );
    }

    @PostMapping("{id}/entrevistas")
    @ResponseStatus(HttpStatus.CREATED)
    public EmpregoApiResponse<EntrevistaResponse> agendarEntrevista(
            @PathVariable Integer id,
            @RequestBody EntrevistaAgendamentoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Entrevista agendada com sucesso.",
                gestaoCandidaturaService.agendarEntrevista(id, request)
        );
    }

    @PatchMapping("{id}/entrevistas/{entrevistaId}/resultado")
    public EmpregoApiResponse<EntrevistaResponse> registarResultadoEntrevista(
            @PathVariable Integer id,
            @PathVariable Integer entrevistaId,
            @RequestBody EntrevistaResultadoRequest request
    ) {
        return EmpregoApiResponse.sucesso(
                "Resultado da entrevista registado com sucesso.",
                gestaoCandidaturaService.registarResultadoEntrevista(id, entrevistaId, request)
        );
    }
}
