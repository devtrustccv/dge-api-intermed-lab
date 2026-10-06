package cv.dge.dge_api_intermed_lab.application.document.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;

public interface DocumentService {

    String save(DocRelacaoDTO dto);

    String saveReclamcao(DocRelacaoDTO dto);


    String gerarLinkPublico(String path);


}
