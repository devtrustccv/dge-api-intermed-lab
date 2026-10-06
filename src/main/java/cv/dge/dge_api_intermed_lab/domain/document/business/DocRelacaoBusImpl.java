package cv.dge.dge_api_intermed_lab.domain.document.business;

import cv.dge.dge_api_intermed_lab.infrastructure.document.DocRelacaoEntity;
import cv.dge.dge_api_intermed_lab.infrastructure.document.repository.DocRelacaoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocRelacaoBusImpl implements DocRelacaoBus {

    private final DocRelacaoRepository docRelacaoRepository;

    @Override
    public List<DocRelacaoEntity> findByIdRelacao(Integer idRelacao) {
        return docRelacaoRepository.findByIdRelacao(Long.valueOf(idRelacao));
    }

}
