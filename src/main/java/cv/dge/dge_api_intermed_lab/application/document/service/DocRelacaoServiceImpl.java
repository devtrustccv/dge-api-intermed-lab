package cv.dge.dge_api_intermed_lab.application.document.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.mapper.DocRelacaoMapper;
import cv.dge.dge_api_intermed_lab.domain.document.business.DocRelacaoBus;
import cv.dge.dge_api_intermed_lab.infrastructure.document.DocRelacaoEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocRelacaoServiceImpl implements DocRelacaoService {

    private final DocRelacaoBus docRelacaoBus;
    private final DocRelacaoMapper docRelacaoMapper;

    public List<DocRelacaoDTO> buscarDocsComNomeTipoPorIdRelacao(Integer idRelacao) {
        List<DocRelacaoEntity> docs = docRelacaoBus.findByIdRelacao(idRelacao);
        return docs.stream()
                .map(docRelacaoMapper::toDto)
                .toList();
    }
}
