package cv.dge.dge_api_intermed_lab.application.geografia.service;

import java.util.Optional;

public interface GlobalGeografiaService {

    String buscarNomePorCodigoPais(String id);

    Optional<String> buscarNomePorCodigo(String codigo);
}
