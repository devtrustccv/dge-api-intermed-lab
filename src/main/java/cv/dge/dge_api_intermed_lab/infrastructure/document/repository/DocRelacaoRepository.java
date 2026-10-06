package cv.dge.dge_api_intermed_lab.infrastructure.document.repository;

import cv.dge.dge_api_intermed_lab.infrastructure.document.DocRelacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocRelacaoRepository extends JpaRepository<DocRelacaoEntity, Integer> {


    List<DocRelacaoEntity> findByIdRelacao(Long idRelacao);

}
