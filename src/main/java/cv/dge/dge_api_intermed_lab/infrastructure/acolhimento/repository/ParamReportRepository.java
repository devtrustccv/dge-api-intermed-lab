package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.ParamReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParamReportRepository extends JpaRepository<ParamReport, Integer> {

    Optional<ParamReport> findFirstByOrderByIdDesc();
}
