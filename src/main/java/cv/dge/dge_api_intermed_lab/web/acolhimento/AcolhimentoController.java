package cv.dge.dge_api_intermed_lab.web.acolhimento;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoRequest;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoReporterResponse;
import java.util.LinkedHashMap;
import cv.dge.dge_api_intermed_lab.application.acolhimento.service.AcolhimentoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
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
@RequestMapping("/v1/acolhimentos")
public class AcolhimentoController {

    private static final Pattern CAMPO_TOKEN_PATTERN = Pattern.compile("([^.\\[\\]]+)|\\[(\\d+|[^\\]]+)\\]");
    private static final String PARTE_DADOS = "dados";
    private static final String MSG_DADOS_OBRIGATORIOS =
            "Envie a parte 'dados' em JSON ou os campos individuais em multipart/form-data.";
    private static final String MSG_JSON_INVALIDO = "JSON invalido na parte 'dados'.";
    private static final String MSG_MULTIPART_INVALIDO = "Campos multipart invalidos.";

    private final AcolhimentoService acolhimentoService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AcolhimentoRegistoResponse registar(@RequestBody AcolhimentoRegistoRequest request) {
        return acolhimentoService.registar(request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AcolhimentoRegistoResponse registarComFicheiro(
            @RequestPart(value = PARTE_DADOS, required = false) String dadosJson,
            @RequestParam(required = false) MultiValueMap<String, String> campos,
            @RequestPart(value = "ficheiro", required = false) MultipartFile ficheiro,
            @RequestPart(value = "ficheiros", required = false) List<MultipartFile> ficheiros
    ) {
        return acolhimentoService.registar(converterRequest(dadosJson, campos), juntarFicheiros(ficheiro, ficheiros));
    }

    @GetMapping("reporter/{id}")
    public AcolhimentoReporterResponse buscarParaReporter(@PathVariable("id") Integer idAcolhimento) {
        return acolhimentoService.buscarParaReporter(idAcolhimento);
    }

    private AcolhimentoRegistoRequest converterRequest(String dadosJson, MultiValueMap<String, String> campos) {
        if (dadosJson != null && !dadosJson.isBlank()) {
            try {
                return objectMapper.readValue(dadosJson, AcolhimentoRegistoRequest.class);
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_JSON_INVALIDO, ex);
            }
        }

        if (campos == null || campos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_DADOS_OBRIGATORIOS);
        }

        Map<String, Object> estrutura = construirEstruturaMultipart(campos);

        try {
            return objectMapper.convertValue(estrutura, AcolhimentoRegistoRequest.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_MULTIPART_INVALIDO, ex);
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

    private Map<String, Object> construirEstruturaMultipart(MultiValueMap<String, String> campos) {
        Map<String, Object> estrutura = new LinkedHashMap<>();
        campos.forEach((chave, valores) -> {
            if (ignorarCampo(chave)) {
                return;
            }
            if (valores == null || valores.isEmpty()) {
                adicionarCampo(estrutura, chave, "");
                return;
            }
            valores.forEach(valor -> adicionarCampo(estrutura, chave, valor));
        });
        return estrutura;
    }

    private boolean ignorarCampo(String chave) {
        return chave == null || chave.isBlank() || PARTE_DADOS.equals(chave);
    }

    private void adicionarCampo(Map<String, Object> estrutura, String chave, String valor) {
        List<Object> tokens = tokenizar(chave);
        if (tokens.isEmpty()) {
            return;
        }

        Object atual = estrutura;
        for (int i = 0; i < tokens.size(); i++) {
            Object token = tokens.get(i);
            boolean ultimo = i == tokens.size() - 1;
            Object proximoToken = ultimo ? null : tokens.get(i + 1);

            if (token instanceof String nomeCampo) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapaAtual = (Map<String, Object>) atual;
                if (ultimo) {
                    juntarValorNoMapa(mapaAtual, nomeCampo, valor);
                } else {
                    Object proximo = mapaAtual.get(nomeCampo);
                    if (proximo == null) {
                        proximo = proximoToken instanceof Integer ? new ArrayList<>() : new LinkedHashMap<>();
                        mapaAtual.put(nomeCampo, proximo);
                    }
                    atual = proximo;
                }
                continue;
            }

            Integer indice = (Integer) token;
            @SuppressWarnings("unchecked")
            List<Object> listaAtual = (List<Object>) atual;
            garantirCapacidade(listaAtual, indice + 1);

            if (ultimo) {
                Object existente = listaAtual.get(indice);
                if (existente == null) {
                    listaAtual.set(indice, valor);
                } else if (existente instanceof List<?> listaExistente) {
                    @SuppressWarnings("unchecked")
                    List<Object> listaValores = (List<Object>) listaExistente;
                    listaValores.add(valor);
                } else {
                    List<Object> listaValores = new ArrayList<>();
                    listaValores.add(existente);
                    listaValores.add(valor);
                    listaAtual.set(indice, listaValores);
                }
            } else {
                Object proximo = listaAtual.get(indice);
                if (proximo == null) {
                    proximo = proximoToken instanceof Integer ? new ArrayList<>() : new LinkedHashMap<>();
                    listaAtual.set(indice, proximo);
                }
                atual = proximo;
            }
        }
    }

    private void juntarValorNoMapa(Map<String, Object> mapaAtual, String nomeCampo, String valor) {
        Object existente = mapaAtual.get(nomeCampo);
        if (existente == null) {
            mapaAtual.put(nomeCampo, valor);
            return;
        }
        if (existente instanceof List<?> listaExistente) {
            @SuppressWarnings("unchecked")
            List<Object> listaValores = (List<Object>) listaExistente;
            listaValores.add(valor);
            return;
        }
        List<Object> listaValores = new ArrayList<>();
        listaValores.add(existente);
        listaValores.add(valor);
        mapaAtual.put(nomeCampo, listaValores);
    }

    private void garantirCapacidade(List<Object> listaAtual, int tamanhoMinimo) {
        while (listaAtual.size() < tamanhoMinimo) {
            listaAtual.add(null);
        }
    }

    private List<Object> tokenizar(String chave) {
        List<Object> tokens = new ArrayList<>();
        Matcher matcher = CAMPO_TOKEN_PATTERN.matcher(chave);
        while (matcher.find()) {
            String token = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (token == null || token.isBlank()) {
                continue;
            }
            if (matcher.group(2) != null && token.chars().allMatch(Character::isDigit)) {
                tokens.add(Integer.parseInt(token));
            } else {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
