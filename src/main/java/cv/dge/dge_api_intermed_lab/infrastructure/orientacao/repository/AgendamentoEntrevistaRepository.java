package cv.dge.dge_api_intermed_lab.infrastructure.orientacao.repository;

import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AgendamentoEntrevista;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoEntrevistaRepository extends JpaRepository<AgendamentoEntrevista, Integer> {

    Optional<AgendamentoEntrevista> findFirstByIdAcolhimentoOrderByDateCreateDescIdDesc(Integer idAcolhimento);
}
