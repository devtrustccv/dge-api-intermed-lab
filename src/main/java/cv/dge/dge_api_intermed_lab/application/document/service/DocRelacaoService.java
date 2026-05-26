package cv.dge.dge_api_intermed_lab.application.document.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import java.util.List;

public interface DocRelacaoService {

    List<DocRelacaoDTO> buscarDocsComNomeTipoPorIdRelacao(Integer idRelacao);
}
