package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.AcolhimentoServico;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcolhimentoServicoRepository extends JpaRepository<AcolhimentoServico, Integer> {

    Optional<AcolhimentoServico> findFirstByIdEntrevistaOrderByIdDesc(Integer idEntrevista);

    Optional<AcolhimentoServico> findFirstByIdAcolhimentoOrderByIdDesc(Integer idAcolhimento);
}
