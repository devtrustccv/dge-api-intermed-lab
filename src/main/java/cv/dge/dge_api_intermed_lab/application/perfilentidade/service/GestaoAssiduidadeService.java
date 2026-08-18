package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeValidacaoRequest;
import java.util.List;

public interface GestaoAssiduidadeService {

    List<AssiduidadeEstagiarioListaResponse> listar(AssiduidadeEstagiarioFiltro filtro);

    AssiduidadeEstagiarioDetalheResponse buscarPorId(Integer id, Integer entidadeId);

    AssiduidadeEstagiarioDetalheResponse validar(Integer id, Integer entidadeId, AssiduidadeValidacaoRequest request);

    List<AssiduidadeEstagiarioSelectResponse> listarEstagiariosParaFiltro(Integer entidadeId);

    List<AssiduidadeOfertaSelectResponse> listarOfertasParaFiltro(Integer entidadeId);
}
