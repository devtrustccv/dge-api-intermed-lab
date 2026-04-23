package cv.dge.dge_api_intermed_lab.application.document.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.document.dto.PublicUrlResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.DocRelacaoEntity;
import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.repository.DocRelacaoRepository;
import cv.dge.dge_api_intermed_lab.utils.RestClientHelper;
import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {

   

    @Autowired
    private DocRelacaoRepository docRelacaoRepository;

 

    private final RestClientHelper restClientHelper;

    @Value("${api.base.service.url}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    public DocumentServiceImpl(RestClientHelper restClientHelper) {
        this.restClientHelper = restClientHelper;
    }

    public String save(DocRelacaoDTO dto) {
        String apiUrl = url + "/documentos";

        String nProcesso = dto.getNProcesso() != null && !dto.getNProcesso().isBlank()
                ? dto.getNProcesso()
                : "SEM-PROCESSO";

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tipoRelacao", dto.getTipoRelacao());
        body.add("idRelacao", dto.getIdRelacao());
        body.add("estado", dto.getEstado());
        body.add("idTpDoc", dto.getIdTpDoc());
        body.add("appCode", dto.getAppCode());
        body.add("fileName", dto.getFileName());
        String ext = getFileExtension(dto.getFile().getOriginalFilename());

        var path = getPathFile(dto.getFileName(), dto.getTipoRelacao(), dto.getIdRelacao(), nProcesso, dto.getAppCode(), ext);
        body.add("path", path);

        if (dto.getIdTpDoc() != null) {
            body.add("idTpDoc", dto.getIdTpDoc());
        }

        MultipartFile file = dto.getFile();
        if (file != null && !file.isEmpty()) {
            try {
                ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                body.add("file", fileResource);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar o arquivo", e);
            }
        }

        ResponseEntity<String> response = restClientHelper.sendRequest(
                apiUrl,
                HttpMethod.POST,
                body,
                String.class,
                headersMap
        );
        System.out.println("documento");

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Documento salvo com sucesso!");
        } else {
            System.err.println("Erro ao salvar documento: " + response.getBody());
        }

        return path;
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
    public List<DocumentoResponseDTO> getDocumentosPorRelacao(Integer idRelacao, String tipoRelacao, String appCode) {
        List<DocRelacaoEntity> documentosEntity = docRelacaoRepository.findByIdRelacaoAndTipoRelacaoAndAppCode(
                Long.valueOf(idRelacao), tipoRelacao, appCode);

        List<DocumentoResponseDTO> documentosDTO = new ArrayList<>();

        for (DocRelacaoEntity entity : documentosEntity) {
            DocumentoResponseDTO dto = convertToDTO(entity);
            documentosDTO.add(dto);
        }

        return documentosDTO;
    }

    private DocumentoResponseDTO convertToDTO(DocRelacaoEntity entity) {
        DocumentoResponseDTO dto = new DocumentoResponseDTO();
        dto.setId(Long.valueOf(entity.getId()));
        dto.setFileName(entity.getFileName());
        dto.setPath(entity.getPath());
        dto.setTipoRelacao(entity.getTipoRelacao());
        dto.setIdRelacao(Math.toIntExact(entity.getIdRelacao()));
        dto.setIdTpDoc(String.valueOf(entity.getIdTpDoc()));
        dto.setEstado(entity.getEstado());
        dto.setAppCode(entity.getAppCode());

        String previewUrl = buildPreviewUrl(entity.getPath(), false);
        dto.setPreviewUrl(previewUrl);

        return dto;
    }

    private String buildPreviewUrl(String path, boolean download) {
        try {
            if (path == null || path.isEmpty()) {
                return "";
            }

            String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString());
            return url + "/documentos/preview-by-path"
                    + "?path=" + encodedPath
                    + "&download=" + download;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Erro ao codificar URL", e);
        }
    }

    public String gerarLinkPublico(String path) {
        String urls = url + "/documentos/public-url?file_path=" + path;

        PublicUrlResponse response =
                restTemplate.getForObject(urls, PublicUrlResponse.class);

        return response.getUrl();
    }

    public String saveReclamcao(DocRelacaoDTO dto) {
        String apiUrl = url + "/documentos";

        String nProcesso = dto.getNProcesso() != null && !dto.getNProcesso().isBlank()
                ? dto.getNProcesso()
                : "SEM-PROCESSO";

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("tipoRelacao", dto.getTipoRelacao());
        body.add("idRelacao", dto.getIdRelacao());
        body.add("estado", dto.getEstado());
        body.add("idTpDoc", dto.getIdTpDoc());
        body.add("appCode", dto.getAppCode());
        body.add("fileName", dto.getFileName());
        String ext = getFileExtension(dto.getFile().getOriginalFilename());

        var path = getPathFile(dto.getFileName(), dto.getTipoRelacao(), dto.getIdRelacao(), nProcesso, dto.getAppCode(), ext);
        body.add("path", path);

        if (dto.getIdTpDoc() != null) {
            body.add("idTpDoc", dto.getIdTpDoc());
        }

        MultipartFile file = dto.getFile();
        if (file != null && !file.isEmpty()) {
            try {
                ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                body.add("file", fileResource);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar o arquivo", e);
            }
        }

        ResponseEntity<String> response = restClientHelper.sendRequest(
                apiUrl,
                HttpMethod.POST,
                body,
                String.class,
                headersMap
        );
        System.out.println("documento");

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Documento salvo com sucesso!");
        } else {
            System.err.println("Erro ao salvar documento: " + response.getBody());
        }

        return path;
    }
}
