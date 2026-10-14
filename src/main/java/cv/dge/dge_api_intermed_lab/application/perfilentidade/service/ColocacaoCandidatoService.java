package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoOfertaSelectResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ColocacaoCandidatoService {

    List<ColocacaoCandidatoListaResponse> listar(ColocacaoCandidatoFiltro filtro);

    ColocacaoCandidatoResponse buscarPorId(Integer id, Integer entidadeId);

    List<ColocacaoOfertaSelectResponse> listarOfertasPorTipoEEntidade(String tipoOferta, Integer entidadeId);

    List<ColocacaoCandidatoSelectResponse> listarCandidatos(Integer entidadeId, Integer ofertaId);

    ColocacaoCandidatoResponse criar(Integer entidadeId, ColocacaoCandidatoRequest request);

    ColocacaoCandidatoResponse criar(
            Integer entidadeId,
            ColocacaoCandidatoRequest request,
            MultipartFile contrato
    );

    ColocacaoCandidatoResponse atualizar(Integer id, Integer entidadeId, ColocacaoCandidatoRequest request);

    ColocacaoCandidatoResponse atualizar(
            Integer id,
            Integer entidadeId,
            ColocacaoCandidatoRequest request,
            MultipartFile contrato
    );

    ColocacaoCandidatoResponse remover(Integer id, Integer entidadeId, ColocacaoCandidatoRemoverRequest request);
}
