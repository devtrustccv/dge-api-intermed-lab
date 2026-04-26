package cv.dge.dge_api_intermed_lab.application.geografia.service;

import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.GlobalGeografia;
import cv.dge.dge_api_intermed_lab.infrastructure.tertiary.repository.GlobalGeografiaRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GlobalGeografiaService {

    private final GlobalGeografiaRepository repository;

    public GlobalGeografiaService(GlobalGeografiaRepository repository) {
        this.repository = repository;
    }

    public String buscarNomePorCodigoPais(String id) {
        return buscarNomePorCodigo(id).orElse(null);
    }

    public Optional<String> buscarNomePorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }
        String codigoLimpo = codigo.trim();
        return repository.findById(codigoLimpo)
                .or(() -> repository.findFirstByCodigoIgnoreCase(codigoLimpo))
                .map(GlobalGeografia::getNome)
                .filter(nome -> nome != null && !nome.trim().isEmpty());
    }

}

