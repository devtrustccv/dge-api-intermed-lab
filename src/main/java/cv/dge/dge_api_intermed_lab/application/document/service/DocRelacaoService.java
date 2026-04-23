package cv.dge.dge_api_intermed_lab.application.document.service;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.DocRelacaoEntity;
import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.repository.DocRelacaoRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocRelacaoService {

    private final DocRelacaoRepository docRelacaoRepository;

    public List<DocRelacaoDTO> buscarDocsComNomeTipoPorIdRelacao(Integer idRelacao) {
        List<DocRelacaoEntity> docs = docRelacaoRepository.findByIdRelacao(Long.valueOf(idRelacao));
        List<DocRelacaoDTO> dtos = new ArrayList<>();

        for (DocRelacaoEntity doc : docs) {
            DocRelacaoDTO dto = new DocRelacaoDTO();
            dto.setIdRelacao(doc.getIdRelacao() == null ? null : doc.getIdRelacao().intValue());
            dto.setTipoRelacao(doc.getTipoRelacao());
            dto.setIdTpDoc(doc.getIdTpDoc() == null ? null : doc.getIdTpDoc().toString());
            dto.setEstado(doc.getEstado());
            dto.setName(doc.getName());
            dto.setPath(doc.getPath());
            dto.setFileName(doc.getFileName());
            dto.setAppCode(doc.getAppCode());
            dtos.add(dto);
        }

        return dtos;
    }
}
