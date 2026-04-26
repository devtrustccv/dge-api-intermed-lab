package dge.dge_equiv_api.application.geografia.service;

import dge.dge_equiv_api.infrastructure.tertiary.GlobalGeografia;
import dge.dge_equiv_api.infrastructure.tertiary.repository.GlobalGeografiaRepository;
import org.springframework.stereotype.Service;

@Service
public class GlobalGeografiaService {

    private final GlobalGeografiaRepository repository;

    public GlobalGeografiaService(GlobalGeografiaRepository repository) {
        this.repository = repository;
    }

    public String buscarNomePorCodigoPais(String id) {
        return repository.findById(id)
                .map(GlobalGeografia::getNome)
                .orElse(null);
    }
}

