package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoOfertaSelectResponse;
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
