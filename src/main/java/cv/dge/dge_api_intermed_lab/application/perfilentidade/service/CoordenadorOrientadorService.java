package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador.CoordenadorOrientadorFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador.CoordenadorOrientadorListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador.CoordenadorOrientadorRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador.CoordenadorOrientadorRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador.CoordenadorOrientadorResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador.PessoaGlobalResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.vaga.VagaListaResponse;
import java.util.List;

public interface CoordenadorOrientadorService {

    List<CoordenadorOrientadorListaResponse> listar(CoordenadorOrientadorFiltro filtro);

    CoordenadorOrientadorResponse buscarPorId(Integer id, Integer entidadeId);

    PessoaGlobalResponse buscarPessoa(Integer entidadeId, String tipoDocumento, String numeroDocumento);

    CoordenadorOrientadorResponse criar(Integer entidadeId, CoordenadorOrientadorRequest request);

    CoordenadorOrientadorResponse atualizar(Integer id, Integer entidadeId, CoordenadorOrientadorRequest request);

    CoordenadorOrientadorResponse remover(Integer id, Integer entidadeId, CoordenadorOrientadorRemoverRequest request);

    List<VagaListaResponse> listarOfertasAssociadas(Integer id, Integer entidadeId, String search);

    default List<VagaListaResponse> listarOfertasAssociadas(Integer id, Integer entidadeId) {
        return listarOfertasAssociadas(id, entidadeId, null);
    }
}
