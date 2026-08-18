package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.*;
import java.util.List;

public interface GestaoAvaliacaoEstagiarioService {
    List<AvaliacaoEstagiarioListaResponse> listar(AvaliacaoEstagiarioFiltro filtro);
    AvaliacaoEstagiarioDetalheResponse buscarPorId(Integer id, Integer entidadeId);
    AvaliacaoEstagiarioDetalheResponse criar(Integer entidadeId, AvaliacaoEstagiarioRequest request);
    AvaliacaoEstagiarioDetalheResponse atualizar(Integer id, Integer entidadeId, AvaliacaoEstagiarioRequest request);
    List<AvaliacaoEstagiarioSelectResponse> listarEstagiarios(Integer entidadeId);
}
