package cv.dge.dge_api_intermed_lab.application.document.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.utils.RestClientHelper;
import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final String DOCUMENTOS_ENDPOINT = "/documentos";
    private static final String DEFAULT_N_PROCESSO = "SEM-PROCESSO";
    private static final String DEFAULT_DOCUMENT_TYPE = "application/pdf";

    private final RestClientHelper restClientHelper;

    @Value("${api.base.service.url}")
    private String url;

    @Value("${doc.open}")
    private String docOpen;

    public DocumentServiceImpl(RestClientHelper restClientHelper) {
        this.restClientHelper = restClientHelper;
    }

    @Override
    public String save(DocRelacaoDTO dto) {
        return guardarDocumento(dto);
    }

    @Override
    public String saveReclamcao(DocRelacaoDTO dto) {
        return guardarDocumento(dto);
    }

    private String guardarDocumento(DocRelacaoDTO dto) {
        String apiUrl = url + DOCUMENTOS_ENDPOINT;
        Map<String, String> headersMap = criarHeadersMultipart();
        MultiValueMap<String, Object> body = criarBodyDocumento(dto);
        ResponseEntity<String> response = enviarDocumento(apiUrl, body, headersMap);

        registarResultadoUpload(response);
        return body.getFirst("path").toString();
    }

    private MultiValueMap<String, Object> criarBodyDocumento(DocRelacaoDTO dto) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tipoRelacao", dto.getTipoRelacao());
        body.add("idRelacao", dto.getIdRelacao());
        adicionarSePreenchido(body, "estado", dto.getEstado());
        if (dto.getIdTpDoc() != null) {
            body.add("idTpDoc", dto.getIdTpDoc());
        }
        body.add("appCode", dto.getAppCode());
        adicionarSePreenchido(body, "fileName", dto.getFileName());
        body.add("path", resolverPathDocumento(dto));

        MultipartFile file = dto.getFile();
        if (file != null && !file.isEmpty()) {
            body.add("file", criarRecursoArquivo(file));
        }
        return body;
    }

    private void adicionarSePreenchido(MultiValueMap<String, Object> body, String nome, String valor) {
        if (valor != null && !valor.isBlank()) {
            body.add(nome, valor);
        }
    }

    private ResponseEntity<String> enviarDocumento(
            String apiUrl,
            MultiValueMap<String, Object> body,
            Map<String, String> headersMap
    ) {
        return restClientHelper.sendRequest(
                apiUrl,
                HttpMethod.POST,
                body,
                String.class,
                headersMap
        );
    }

    private Map<String, String> criarHeadersMultipart() {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);
        return headersMap;
    }

    private String resolverPathDocumento(DocRelacaoDTO dto) {
        if (dto.getPath() != null && !dto.getPath().isBlank()) {
            return dto.getPath();
        }

        String ext = getFileExtension(dto.getFile().getOriginalFilename());
        String nProcesso = dto.getNProcesso() != null && !dto.getNProcesso().isBlank()
                ? dto.getNProcesso()
                : DEFAULT_N_PROCESSO;

        return getPathFile(dto.getFileName(), dto.getTipoRelacao(), dto.getIdRelacao(), nProcesso, dto.getAppCode(), ext);
    }

    private ByteArrayResource criarRecursoArquivo(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível processar o ficheiro.", e);
        }
    }

    private void registarResultadoUpload(ResponseEntity<String> response) {
        System.out.println("documento");

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Documento salvo com sucesso!");
        } else {
            System.err.println("Erro ao salvar documento: " + response.getBody());
        }
    }

    public String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }

    public String getPathFile(String fileName, String tipoRelacao, Integer idRelacao, String nprocesso, String appCode, String ext) {
        System.out.println("ext " + ext);
        return appCode + "/" + LocalDateTime.now().getYear() + "/processos/" + tipoRelacao + "/" + nprocesso + "/" + idRelacao + "/" + fileName + "." + ext;
    }

    public static String getBasePathForProcess(String appDad, @NonNull String processTypeKey, @Nullable String processInstanceID, @Nullable String taskKey) {
        var thisYear = String.valueOf(LocalDateTime.now().getYear());
        var task = (taskKey == null || taskKey.isEmpty() ? "" : taskKey + "/");
        var processId = (processInstanceID == null || processInstanceID.isEmpty() ? "" : processInstanceID + "/");

        return appDad + "/" + thisYear + "/processos/" + processTypeKey + "/" + processId + task;
    }

    @Override
    public String gerarLinkPublico(String pathOuUrl) {
        if (pathOuUrl == null || pathOuUrl.isBlank()) {
            return "";
        }

        String valor = pathOuUrl.trim();
        if (ehUrlAbsoluta(valor)) {
            String pathExtraido = extrairParametroPathUrl(valor);
            if (pathExtraido == null || pathExtraido.isBlank()) {
                return valor;
            }
            valor = pathExtraido;
        }

        return appendQueryParam(docOpen, "path_url", valor)
                + "&type=" + resolverMediaType(valor);
    }

    private String appendQueryParam(String baseUrl, String paramName, String paramValue) {
        String separator = baseUrl.contains("?")
                ? (baseUrl.endsWith("?") || baseUrl.endsWith("&") ? "" : "&")
                : "?";

        // O Document_viewer do IGRP recebe o caminho no mesmo formato usado pelo SGF.
        // Em particular, as barras do caminho não devem ser convertidas para %2F.
        return baseUrl + separator + paramName + "=" + paramValue;
    }

    private boolean ehUrlAbsoluta(String valor) {
        return valor.regionMatches(true, 0, "http://", 0, 7)
                || valor.regionMatches(true, 0, "https://", 0, 8);
    }

    private String extrairParametroPathUrl(String urlDocumento) {
        try {
            String query = URI.create(urlDocumento).getRawQuery();
            if (query == null) {
                return null;
            }

            for (String parametro : query.split("&")) {
                int separador = parametro.indexOf('=');
                String nome = separador >= 0 ? parametro.substring(0, separador) : parametro;
                if ("path_url".equals(URLDecoder.decode(nome, StandardCharsets.UTF_8))) {
                    String valor = separador >= 0 ? parametro.substring(separador + 1) : "";
                    return URLDecoder.decode(valor, StandardCharsets.UTF_8);
                }
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return null;
    }

    private String resolverMediaType(String path) {
        return MediaTypeFactory.getMediaType(path)
                .map(MediaType::toString)
                .orElse(DEFAULT_DOCUMENT_TYPE);
    }

}
