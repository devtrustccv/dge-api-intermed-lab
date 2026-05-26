package cv.dge.dge_api_intermed_lab.domain.geografia.business;

import cv.dge.dge_api_intermed_lab.infrastructure.geografia.GlobalGeografia;
import cv.dge.dge_api_intermed_lab.infrastructure.geografia.repository.GlobalGeografiaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlobalGeografiaBusImpl implements GlobalGeografiaBus {

    private final GlobalGeografiaRepository repository;

    @Override
    public Optional<GlobalGeografia> findByCodigo(String codigo) {
        return repository.findById(codigo)
                .or(() -> repository.findFirstByCodigoIgnoreCase(codigo));
    }
}
