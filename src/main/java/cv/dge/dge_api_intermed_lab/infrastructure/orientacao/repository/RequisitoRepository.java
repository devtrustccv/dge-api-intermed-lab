package cv.dge.dge_api_intermed_lab.infrastructure.orientacao.repository;

import cv.dge.dge_api_intermed_lab.domain.orientacao.model.Requisito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisitoRepository extends JpaRepository<Requisito, Integer> {

    List<Requisito> findAllByEstadoIgnoreCaseOrderByDateCreateDescIdDesc(String estado);

    List<Requisito> findAllByEstadoIgnoreCaseAndTipoServicoIgnoreCaseOrderByDateCreateDescIdDesc(
            String estado,
            String tipoServico
    );
}
