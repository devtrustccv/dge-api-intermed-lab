package cv.dge.dge_api_intermed_lab.infrastructure.tertiary.repository;

import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.DocRelacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocRelacaoRepository extends JpaRepository<DocRelacaoEntity, Integer> {


    List<DocRelacaoEntity> findByIdRelacao(Long idRelacao);

    @Query("SELECT d FROM DocRelacaoEntity d WHERE d.idRelacao = :idRelacao AND d.tipoRelacao = :tipoRelacao AND d.appCode = :appCode")
    List<DocRelacaoEntity> findByIdRelacaoAndTipoRelacaoAndAppCode(
            @Param("idRelacao") Long idRelacao,
            @Param("tipoRelacao") String tipoRelacao,
            @Param("appCode") String appCode);

}
