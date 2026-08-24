package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaFormularioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaVagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagasResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.ConsultaVagaService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/v1/perfil-candidato/vagas")
public class PerfilCandidatoVagaController {

    private final ConsultaVagaService consultaVagaService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public PerfilCandidatoApiResponse<ConsultaVagasResponse> listar(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String tipoOferta,
            @RequestParam(required = false) Integer entidadeId,
            @RequestParam(required = false) String ilha,
            @RequestParam(required = false) String concelho,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String codigoReferencia,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String pesquisa
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Ofertas disponíveis carregadas com sucesso.",
                consultaVagaService.listar(new ConsultaVagaFiltro(
                        pessoaId,
                        tipoOferta,
                        entidadeId,
                        ilha,
                        concelho,
                        estado,
                        codigoReferencia,
                        dataInicio,
                        dataFim,
                        pesquisa
                ))
        );
    }

    @GetMapping("opcoes")
    public PerfilCandidatoApiResponse<ConsultaVagaOpcoesResponse> listarOpcoes(
            @RequestParam(required = false) Long ilhaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Opções da consulta de ofertas carregadas com sucesso.",
                consultaVagaService.listarOpcoes(ilhaId)
        );
    }

    @GetMapping("{ofertaId}")
    public PerfilCandidatoApiResponse<ConsultaVagaDetalheResponse> buscarPorId(
            @PathVariable Integer ofertaId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes da oferta carregados com sucesso.",
                consultaVagaService.buscarPorId(ofertaId, pessoaId)
        );
    }

    @GetMapping("{ofertaId}/candidatura")
    public PerfilCandidatoApiResponse<CandidaturaVagaFormularioResponse> buscarFormularioCandidatura(
            @PathVariable Integer ofertaId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Dados para a candidatura carregados com sucesso.",
                consultaVagaService.buscarFormularioCandidatura(ofertaId, pessoaId)
        );
    }

    @PostMapping(value = "{ofertaId}/candidaturas", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<CandidaturaVagaResponse> candidatarComCurriculoAtual(
            @PathVariable Integer ofertaId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody CandidaturaVagaRequest request
    ) {
        return respostaCandidatura(
                consultaVagaService.candidatar(ofertaId, pessoaId, request, null, List.of())
        );
    }

    @PostMapping(value = "{ofertaId}/candidaturas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<CandidaturaVagaResponse> candidatarComDocumentos(
            @PathVariable Integer ofertaId,
            @RequestParam(required = false) Long pessoaId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "novaVersaoCv", required = false) MultipartFile novaVersaoCv,
            @RequestPart(value = "curriculo", required = false) MultipartFile curriculo,
            @RequestPart(value = "outrosDocumentos", required = false) List<MultipartFile> outrosDocumentos
    ) {
        MultipartFile ficheiroCurriculo = temConteudo(novaVersaoCv) ? novaVersaoCv : curriculo;
        return respostaCandidatura(consultaVagaService.candidatar(
                ofertaId,
                pessoaId,
                converterDados(dadosJson),
                ficheiroCurriculo,
                outrosDocumentos
        ));
    }

    private PerfilCandidatoApiResponse<CandidaturaVagaResponse> respostaCandidatura(
            CandidaturaVagaResponse candidatura
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Candidatura submetida com sucesso.",
                candidatura
        );
    }

    private CandidaturaVagaRequest converterDados(String dadosJson) {
        if (dadosJson == null || dadosJson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha os dados da candidatura antes de confirmar."
            );
        }
        try {
            return objectMapper.readValue(dadosJson, CandidaturaVagaRequest.class);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível interpretar os dados da candidatura. Reveja os campos e tente novamente.",
                    ex
            );
        }
    }

    private boolean temConteudo(MultipartFile ficheiro) {
        return ficheiro != null && !ficheiro.isEmpty();
    }
}
