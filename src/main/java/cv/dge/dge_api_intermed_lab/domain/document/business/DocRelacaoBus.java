package cv.dge.dge_api_intermed_lab.domain.document.business;

import cv.dge.dge_api_intermed_lab.infrastructure.document.DocRelacaoEntity;
import java.util.List;

public interface DocRelacaoBus {

    List<DocRelacaoEntity> findByIdRelacao(Integer idRelacao);

    List<DocRelacaoEntity> findByRelacao(Integer idRelacao, String tipoRelacao, String appCode);
}
