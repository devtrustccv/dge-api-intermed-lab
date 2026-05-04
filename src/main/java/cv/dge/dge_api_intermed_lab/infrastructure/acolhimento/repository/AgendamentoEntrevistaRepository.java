package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AgendamentoEntrevista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoEntrevistaRepository extends JpaRepository<AgendamentoEntrevista, Integer> {
}
