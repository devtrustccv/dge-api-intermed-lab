package cv.dge.dge_api_intermed_lab.web.orientacao;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoRequest;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.service.OrientacaoServicoService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orientacao/servicos")
public class OrientacaoServicoController {

    private final OrientacaoServicoService orientacaoServicoService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OrientacaoServicoResponse guardar(@RequestBody OrientacaoServicoRequest request) {
        return orientacaoServicoService.guardar(request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OrientacaoServicoResponse guardarComFicheiros(
            @RequestPart("dados") String dadosJson,
            @RequestPart(value = "ficheiro", required = false) MultipartFile ficheiro,
            @RequestPart(value = "ficheiros", required = false) List<MultipartFile> ficheiros
    ) {
        return orientacaoServicoService.guardar(converterRequest(dadosJson), juntarFicheiros(ficheiro, ficheiros));
    }

    @GetMapping("entrevista/{idEntrevista}")
    public OrientacaoServicoResponse buscarPorEntrevista(
            @PathVariable Integer idEntrevista,
            @RequestParam(value = "tipoServico", required = false) String tipoServico
    ) {
        return orientacaoServicoService.buscarPorEntrevistaETipoServico(idEntrevista, tipoServico);
    }

    private OrientacaoServicoRequest converterRequest(String dadosJson) {
        try {
            return objectMapper.readValue(dadosJson, OrientacaoServicoRequest.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON invalido na parte 'dados'.", ex);
        }
    }

    private List<MultipartFile> juntarFicheiros(MultipartFile ficheiro, List<MultipartFile> ficheiros) {
        List<MultipartFile> todos = new ArrayList<>();
        if (ficheiros != null) {
            todos.addAll(ficheiros);
        }
        if (ficheiro != null) {
            todos.add(ficheiro);
        }
        return todos;
    }
}
