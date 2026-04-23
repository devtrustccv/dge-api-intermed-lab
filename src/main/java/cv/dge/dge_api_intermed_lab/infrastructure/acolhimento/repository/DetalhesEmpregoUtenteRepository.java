package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.DetalhesEmpregoUtente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalhesEmpregoUtenteRepository extends JpaRepository<DetalhesEmpregoUtente, Integer> {
}
