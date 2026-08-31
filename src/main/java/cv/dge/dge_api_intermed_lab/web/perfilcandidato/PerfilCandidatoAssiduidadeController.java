package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaAssiduidadeService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/perfil-candidato/assiduidades")
public class PerfilCandidatoAssiduidadeController {

    private final MinhaAssiduidadeService assiduidadeService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public PerfilCandidatoApiResponse<List<MinhaAssiduidadeListaResponse>> listar(
            @RequestParam(required = false) Long pessoaId,
            @RequestParam(required = false) String tipoAssiduidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Registos de assiduidade carregados com sucesso.",
                assiduidadeService.listar(new MinhaAssiduidadeFiltro(
                        pessoaId,
                        tipoAssiduidade,
                        estado,
                        dataInicio,
                        dataFim
                ))
        );
    }

    @GetMapping("opcoes")
    public PerfilCandidatoApiResponse<MinhaAssiduidadeOpcoesResponse> listarOpcoes() {
        return PerfilCandidatoApiResponse.sucesso(
                "Opções da assiduidade carregadas com sucesso.",
                assiduidadeService.listarOpcoes()
        );
    }

    @GetMapping("{assiduidadeId}")
    public PerfilCandidatoApiResponse<MinhaAssiduidadeDetalheResponse> buscarPorId(
            @PathVariable Integer assiduidadeId,
            @RequestParam(required = false) Long pessoaId
    ) {
        return PerfilCandidatoApiResponse.sucesso(
                "Detalhes da assiduidade carregados com sucesso.",
                assiduidadeService.buscarPorId(assiduidadeId, pessoaId)
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilCandidatoApiResponse<MinhaAssiduidadeDetalheResponse> criar(
            @RequestParam(required = false) Long pessoaId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "comprovativo", required = false) MultipartFile comprovativo,
            @RequestPart(value = "anexoComprovativo", required = false) MultipartFile anexoComprovativo,
            @RequestPart(value = "novoAnexoComprovativo", required = false) MultipartFile novoAnexoComprovativo
    ) {
        MinhaAssiduidadeDetalheResponse assiduidade = assiduidadeService.criar(
                pessoaId,
                converterDados(dadosJson),
                primeiroComConteudo(comprovativo, anexoComprovativo, novoAnexoComprovativo)
        );
        return PerfilCandidatoApiResponse.sucesso(
                "Assiduidade registada com sucesso.",
                assiduidade
        );
    }

    @PutMapping(value = "{assiduidadeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PerfilCandidatoApiResponse<MinhaAssiduidadeDetalheResponse> atualizar(
            @PathVariable Integer assiduidadeId,
            @RequestParam(required = false) Long pessoaId,
            @RequestPart(value = "dados", required = false) String dadosJson,
            @RequestPart(value = "novoAnexoComprovativo", required = false) MultipartFile novoAnexoComprovativo,
            @RequestPart(value = "novoComprovativo", required = false) MultipartFile novoComprovativo,
            @RequestPart(value = "anexoComprovativo", required = false) MultipartFile anexoComprovativo,
            @RequestPart(value = "comprovativo", required = false) MultipartFile comprovativo
    ) {
        MinhaAssiduidadeDetalheResponse assiduidade = assiduidadeService.atualizar(
                assiduidadeId,
                pessoaId,
                converterDados(dadosJson),
                primeiroComConteudo(novoAnexoComprovativo, novoComprovativo, anexoComprovativo, comprovativo)
        );
        return PerfilCandidatoApiResponse.sucesso(
                "Assiduidade atualizada com sucesso.",
                assiduidade
        );
    }

    private MinhaAssiduidadeRequest converterDados(String dadosJson) {
        if (dadosJson == null || dadosJson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preencha os dados da assiduidade antes de guardar."
            );
        }
        try {
            return objectMapper.readValue(dadosJson, MinhaAssiduidadeRequest.class);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível interpretar os dados da assiduidade. Reveja os campos e tente novamente.",
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
