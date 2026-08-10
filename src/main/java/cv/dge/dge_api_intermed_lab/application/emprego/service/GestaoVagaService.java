package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaDuplicacaoResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaEstadoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaColaboradorSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaValidacaoRequest;
import java.util.List;

public interface GestaoVagaService {

    List<VagaListaResponse> listar(VagaFiltro filtro);

    List<VagaColaboradorSelectResponse> listarColaboradores(String tipo);

    List<VagaColaboradorSelectResponse> listarOrientadores();

    List<VagaColaboradorSelectResponse> listarCoordenadores();

    VagaResponse buscarPorId(Integer id);

    VagaResponse criar(VagaRequest request);

    VagaResponse criarRascunho(VagaRequest request);

    VagaResponse atualizar(Integer id, VagaRequest request);

    VagaResponse alterarEstado(Integer id, VagaEstadoRequest request);

    VagaResponse validar(Integer id, VagaValidacaoRequest request);

    VagaDuplicacaoResponse prepararDuplicacao(Integer id);
}
