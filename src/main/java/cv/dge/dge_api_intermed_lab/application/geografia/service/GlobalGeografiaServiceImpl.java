package cv.dge.dge_api_intermed_lab.application.geografia.service;

import cv.dge.dge_api_intermed_lab.domain.geografia.business.GlobalGeografiaBus;
import cv.dge.dge_api_intermed_lab.infrastructure.geografia.GlobalGeografia;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobalGeografiaServiceImpl implements GlobalGeografiaService {

    private final GlobalGeografiaBus globalGeografiaBus;

    public String buscarNomePorCodigoPais(String id) {
        return buscarNomePorCodigo(id).orElse(null);
    }

    public Optional<String> buscarNomePorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }
        String codigoLimpo = codigo.trim();
        return globalGeografiaBus.findByCodigo(codigoLimpo)
                .map(GlobalGeografia::getNome)
                .filter(nome -> nome != null && !nome.trim().isEmpty());
    }

}

