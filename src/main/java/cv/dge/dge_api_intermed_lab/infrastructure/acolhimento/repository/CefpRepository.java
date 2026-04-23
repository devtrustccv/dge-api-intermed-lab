package cv.dge.dge_api_intermed_lab.infrastructure.acolhimento.repository;

import cv.dge.dge_api_intermed_lab.domain.acolhimento.model.Cefp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CefpRepository extends JpaRepository<Cefp, Integer> {

    Optional<Cefp> findFirstByOrganizationId(Integer organizationId);

    @Query(value = """
            SELECT c.*
            FROM emprego_t_cefp c
            JOIN emprego_t_cefp_areas_abrangidas a ON a.cefp_id = c.id
            WHERE lower(trim(a.ilha)) = lower(trim(:ilha))
              AND lower(trim(a.concelho)) = lower(trim(:concelho))
              AND (a.status IS NULL OR upper(a.status) NOT IN ('INATIVO', 'INACTIVO', 'I'))
            ORDER BY c.id
            LIMIT 1
            """, nativeQuery = true)
    Optional<Cefp> findFirstByAreaAbrangida(@Param("ilha") String ilha, @Param("concelho") String concelho);
}
