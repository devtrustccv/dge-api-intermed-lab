package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteAcaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteCandidatosOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.ServicoContratanteService;
import java.time.LocalDate;
import java.util.ArrayList;
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
@RequestMapping("/v1/perfil-candidato/prestacoes-servicos/contratante")
public class PerfilCandidatoServicoContratanteController {

    private final ServicoContratanteService servicoService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public PerfilCandidatoApiResponse<List<ServicoContratanteListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String tipoServico,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Serviços a contratar carregados com sucesso.",
                servicoService.listar(new ServicoContratanteFiltro(
                        pessoaId,
                        tipoServico,
                        estado,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("opcoes")
    public PerfilCandidatoApiResponse<ServicoContratanteOpcoesResponse> listarOpcoes(
            @RequestParam(required = false) String ilha,
            @RequestParam(required = false) String concelho
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Opções da prestação de serviços carregadas com sucesso.",
                servicoService.listarOpcoes(ilha, concelho)
        );
    }

    @GetMapping("{servicoId}")
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> buscarPorId(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes do serviço carregados com sucesso.",
                servicoService.buscarPorId(servicoId, pessoaId)
        );
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> criarJson(
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteRequest request
    ) {
        return respostaCriacao(servicoService.criar(pessoaId, request, List.of(), false), false);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> criarMultipart(
            @RequestParam(required = false) Long pessoaId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos,
            @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos
    ) {
        return respostaCriacao(servicoService.criar(
                pessoaId,
                converterDados(dadosJson),
                juntarFicheiros(anexos, documentos),
                false
        ), false);
    }

    @PostMapping(value = "rascunho", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> criarRascunhoJson(
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteRequest request
    ) {
        return respostaCriacao(servicoService.criar(pessoaId, request, List.of(), true), true);
    }

    @PostMapping(value = "rascunho", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> criarRascunhoMultipart(
            @RequestParam(required = false) Long pessoaId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos,
            @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos
    ) {
        return respostaCriacao(servicoService.criar(
                pessoaId,
                converterDados(dadosJson),
                juntarFicheiros(anexos, documentos),
                true
        ), true);
    }

    @PutMapping(value = "{servicoId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> atualizarJson(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteRequest request
    ) {
        return respostaAtualizacao(servicoService.atualizar(
                servicoId,
                pessoaId,
                request,
                List.of()
        ));
    }

    @PutMapping(value = "{servicoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> atualizarMultipart(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "novosAnexos", required = false) List<MultipartFile> novosAnexos,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos,
            @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos
    ) {
        return respostaAtualizacao(servicoService.atualizar(
                servicoId,
                pessoaId,
                converterDados(dadosJson),
                juntarFicheiros(novosAnexos, anexos, documentos)
        ));
    }

    @PatchMapping("{servicoId}/cancelar")
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> cancelar(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteAcaoRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Serviço cancelado com sucesso.",
                servicoService.cancelar(servicoId, pessoaId, utilizador(request))
        );
    }

    @PatchMapping("{servicoId}/remover")
    public PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> remover(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteAcaoRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Serviço removido com sucesso.",
                servicoService.remover(servicoId, pessoaId, utilizador(request))
        );
    }

    @GetMapping("{servicoId}/candidatos")
    public PerfilCandidatoApiResponse<List<ServicoContratanteCandidatoListaResponse>> listarCandidatos(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) Long candidatoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Candidatos do serviço carregados com sucesso.",
                servicoService.listarCandidatos(new ServicoContratanteCandidatoFiltro(
                        servicoId,
                        pessoaId,
                        candidatoId,
                        estado,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("{servicoId}/candidatos/opcoes")
    public PerfilCandidatoApiResponse<ServicoContratanteCandidatosOpcoesResponse> listarOpcoesCandidatos(
            @PathVariable Integer servicoId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Opções dos candidatos carregadas com sucesso.",
                servicoService.listarOpcoesCandidatos(servicoId, pessoaId)
        );
    }

    @PatchMapping("{servicoId}/candidatos/{candidaturaId}/selecionar")
    public PerfilCandidatoApiResponse<ServicoContratanteCandidatoListaResponse> selecionarCandidato(
            @PathVariable Integer servicoId,
            @PathVariable Integer candidaturaId,
            @RequestParam(required = false) Long pessoaId,
            @RequestBody(required = false) ServicoContratanteAcaoRequest request
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Candidato selecionado com sucesso.",
                servicoService.selecionarCandidato(
                        servicoId,
                        candidaturaId,
                        pessoaId,
                        utilizador(request)
                )
        );
    }

    private PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> respostaCriacao(
            ServicoContratanteDetalheResponse servico,
            boolean rascunho
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                rascunho ? "Rascunho guardado com sucesso." : "Serviço registado com sucesso.",
                servico
        );
    }

    private PerfilCandidatoApiResponse<ServicoContratanteDetalheResponse> respostaAtualizacao(
            ServicoContratanteDetalheResponse servico
    ) {
        return PerfilCandidatoApiResponse.sucesso("Serviço atualizado com sucesso.", servico);
    }

    private ServicoContratanteRequest converterDados(String dadosJson) {
        if (dadosJson == null || dadosJson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha os dados do serviço antes de guardar."
            );
        }
        try {
            return objectMapper.readValue(dadosJson, ServicoContratanteRequest.class);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível interpretar os dados do serviço. Reveja os campos e tente novamente.",
                    ex
            );
        }
    }

    @SafeVarargs
    private final List<MultipartFile> juntarFicheiros(List<MultipartFile>... grupos) {
        List<MultipartFile> resultado = new ArrayList<>();
        for (List<MultipartFile> grupo : grupos) {
            if (grupo != null) {
                grupo.stream()
                        .filter(ficheiro -> ficheiro != null && !ficheiro.isEmpty())
                        .forEach(resultado::add);
            }
        }
        return List.copyOf(resultado);
    }

    private String utilizador(ServicoContratanteAcaoRequest request) {
        return request == null ? null : request.utilizador();
    }
}
