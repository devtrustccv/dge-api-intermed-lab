package cv.dge.dge_api_intermed_lab.infrastructure.tertiary.repository;

import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.GlobalGeografia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GlobalGeografiaRepository extends JpaRepository<GlobalGeografia, String> {

    Optional<GlobalGeografia> findById(String id);
}
