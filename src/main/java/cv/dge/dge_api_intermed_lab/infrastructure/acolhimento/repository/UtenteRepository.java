package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Utente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {

    Optional<Utente> findFirstByPessoaId(Integer pessoaId);

    Optional<Utente> findFirstByTipoDocumentoIgnoreCaseAndNumDocumentoIgnoreCase(
            String tipoDocumento,
            String numDocumento
    );

    Optional<Utente> findFirstByNumDocumentoIgnoreCase(String numDocumento);
}
