package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CoordenadorOrientadorResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.PessoaGlobalResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.VagaListaResponse;
import java.util.List;

public interface CoordenadorOrientadorService {

    List<CoordenadorOrientadorListaResponse> listar(CoordenadorOrientadorFiltro filtro);

    CoordenadorOrientadorResponse buscarPorId(Integer id);

    PessoaGlobalResponse buscarPessoa(String tipoDocumento, String numeroDocumento);

    CoordenadorOrientadorResponse criar(CoordenadorOrientadorRequest request);

    CoordenadorOrientadorResponse atualizar(Integer id, CoordenadorOrientadorRequest request);

    CoordenadorOrientadorResponse remover(Integer id, CoordenadorOrientadorRemoverRequest request);

    List<VagaListaResponse> listarOfertasAssociadas(Integer id);
}
