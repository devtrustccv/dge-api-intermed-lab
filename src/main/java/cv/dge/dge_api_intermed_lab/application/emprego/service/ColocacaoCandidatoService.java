package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.ColocacaoOfertaSelectResponse;
import java.util.List;

public interface ColocacaoCandidatoService {

    List<ColocacaoCandidatoListaResponse> listar(ColocacaoCandidatoFiltro filtro);

    ColocacaoCandidatoResponse buscarPorId(Integer id);

    List<ColocacaoOfertaSelectResponse> listarOfertasPorTipoEEntidade(String tipoOferta, Integer entidadeId);

    List<ColocacaoCandidatoSelectResponse> listarCandidatosPorOferta(Integer ofertaId);

    ColocacaoCandidatoResponse criar(ColocacaoCandidatoRequest request);

    ColocacaoCandidatoResponse atualizar(Integer id, ColocacaoCandidatoRequest request);

    ColocacaoCandidatoResponse remover(Integer id, ColocacaoCandidatoRemoverRequest request);
}
