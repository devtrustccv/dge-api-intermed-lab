package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.avaliacao.MinhaAvaliacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.avaliacao.MinhaAvaliacaoListaResponse;
import java.util.List;

public interface MinhaAvaliacaoService {

    List<MinhaAvaliacaoListaResponse> listar(Long pessoaId, String search);

    default List<MinhaAvaliacaoListaResponse> listar(Long pessoaId) {
        return listar(pessoaId, null);
    }

    MinhaAvaliacaoDetalheResponse buscarPorId(Integer avaliacaoId, Long pessoaId);
}
