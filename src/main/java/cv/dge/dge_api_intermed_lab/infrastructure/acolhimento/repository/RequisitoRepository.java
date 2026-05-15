package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Requisito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisitoRepository extends JpaRepository<Requisito, Integer> {

    List<Requisito> findAllByEstadoIgnoreCaseOrderByDateCreateDescIdDesc(String estado);
}
