package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Entidade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntidadeRepository extends JpaRepository<Entidade, Integer> {

    Optional<Entidade> findFirstByNif(Integer nif);
}
