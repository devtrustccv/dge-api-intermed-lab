package cv.dge.dge_api_intermed_lab.application.document.mapper;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.infrastructure.document.DocRelacaoEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocRelacaoMapper {

    public DocRelacaoDTO toUploadDto(
            Integer idRelacao,
            String tipoRelacao,
            String estado,
            String idTpDoc,
            String name,
            String fileName,
            String path,
            String appCode,
            MultipartFile file
    ) {
        return DocRelacaoDTO.builder()
                .idRelacao(idRelacao)
                .tipoRelacao(tipoRelacao)
                .estado(estado)
                .idTpDoc(idTpDoc)
                .name(name)
                .fileName(fileName)
                .path(path)
                .appCode(appCode)
                .file(file)
                .build();
    }

    public DocRelacaoDTO toDto(DocRelacaoEntity entity) {
        DocRelacaoDTO dto = new DocRelacaoDTO();
        dto.setIdRelacao(entity.getIdRelacao() == null ? null : entity.getIdRelacao().intValue());
        dto.setTipoRelacao(entity.getTipoRelacao());
        dto.setIdTpDoc(entity.getIdTpDoc() == null ? null : entity.getIdTpDoc().toString());
        dto.setEstado(entity.getEstado());
        dto.setName(entity.getName());
        dto.setPath(entity.getPath());
        dto.setFileName(entity.getFileName());
        dto.setAppCode(entity.getAppCode());
        return dto;
    }

    public DocumentoResponseDTO toDocumentoResponse(DocRelacaoEntity entity, String previewUrl) {
        DocumentoResponseDTO dto = new DocumentoResponseDTO();
        dto.setId(Long.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setFileName(entity.getFileName());
        dto.setPath(entity.getPath());
        dto.setTipoRelacao(entity.getTipoRelacao());
        dto.setIdRelacao(entity.getIdRelacao() == null ? null : Math.toIntExact(entity.getIdRelacao()));
        dto.setIdTpDoc(entity.getIdTpDoc() == null ? null : String.valueOf(entity.getIdTpDoc()));
        dto.setEstado(entity.getEstado());
        dto.setAppCode(entity.getAppCode());
        dto.setDataCriacao(entity.getDateCreate() == null ? null : entity.getDateCreate().toString());
        dto.setPreviewUrl(previewUrl);
        return dto;
    }
}
