package cv.dge.dge_api_intermed_lab.application.document.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;

import java.util.List;

public interface DocumentService {

    String save(DocRelacaoDTO dto);

    String saveReclamcao(DocRelacaoDTO dto);


    List<DocumentoResponseDTO> getDocumentosPorRelacao(Integer idRelacao, String tipoRelacao, String appCode);

    String gerarLinkPublico(String path);


}
