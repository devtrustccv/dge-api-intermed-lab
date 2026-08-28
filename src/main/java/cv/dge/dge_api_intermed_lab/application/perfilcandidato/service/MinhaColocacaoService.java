package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoListaResponse;
import java.util.List;

public interface MinhaColocacaoService {

    List<MinhaColocacaoListaResponse> listar(Long pessoaId);

    MinhaColocacaoDetalheResponse buscarPorId(Integer colocacaoId, Long pessoaId);
}
