package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoListaResponse;
import java.util.List;

public interface MinhaAvaliacaoService {

    List<MinhaAvaliacaoListaResponse> listar(Long pessoaId);

    MinhaAvaliacaoDetalheResponse buscarPorId(Integer avaliacaoId, Long pessoaId);
}
