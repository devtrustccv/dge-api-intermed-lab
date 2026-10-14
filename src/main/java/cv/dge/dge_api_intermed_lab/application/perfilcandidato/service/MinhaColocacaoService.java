package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.colocacao.MinhaColocacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.colocacao.MinhaColocacaoListaResponse;
import java.util.List;

public interface MinhaColocacaoService {

    List<MinhaColocacaoListaResponse> listar(Long pessoaId, String search);

    default List<MinhaColocacaoListaResponse> listar(Long pessoaId) {
        return listar(pessoaId, null);
    }

    MinhaColocacaoDetalheResponse buscarPorId(Integer colocacaoId, Long pessoaId);
}
