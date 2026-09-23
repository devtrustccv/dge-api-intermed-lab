package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaDuplicacaoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaEstadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaColaboradorSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaValidacaoRequest;
import java.util.List;

public interface GestaoVagaService {

    List<VagaListaResponse> listar(VagaFiltro filtro);

    List<VagaColaboradorSelectResponse> listarColaboradores(Integer entidadeId, String tipo);

    List<VagaColaboradorSelectResponse> listarOrientadores(Integer entidadeId);

    List<VagaColaboradorSelectResponse> listarCoordenadores(Integer entidadeId);

    VagaResponse buscarPorId(Integer id, Integer entidadeId);

    VagaResponse criar(Integer entidadeId, VagaRequest request);

    VagaResponse criarRascunho(Integer entidadeId, VagaRequest request);

    VagaResponse atualizar(Integer id, Integer entidadeId, VagaRequest request);

    VagaResponse alterarEstado(Integer id, Integer entidadeId, VagaEstadoRequest request);

    VagaResponse validar(Integer id, Integer entidadeId, VagaValidacaoRequest request);

    VagaDuplicacaoResponse prepararDuplicacao(Integer id, Integer entidadeId);
}
