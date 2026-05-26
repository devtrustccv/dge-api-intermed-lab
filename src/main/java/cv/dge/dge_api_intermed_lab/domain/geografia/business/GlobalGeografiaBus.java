package cv.dge.dge_api_intermed_lab.domain.geografia.business;

import cv.dge.dge_api_intermed_lab.infrastructure.geografia.GlobalGeografia;
import java.util.Optional;

public interface GlobalGeografiaBus {

    Optional<GlobalGeografia> findByCodigo(String codigo);
}
