package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeValidacaoRequest;
import java.util.List;

public interface GestaoAssiduidadeService {

    List<AssiduidadeEstagiarioListaResponse> listar(AssiduidadeEstagiarioFiltro filtro);

    AssiduidadeEstagiarioDetalheResponse buscarPorId(Integer id, Integer entidadeId);

    AssiduidadeEstagiarioDetalheResponse validar(Integer id, Integer entidadeId, AssiduidadeValidacaoRequest request);

    List<AssiduidadeEstagiarioSelectResponse> listarEstagiariosParaFiltro(Integer entidadeId);

    List<AssiduidadeOfertaSelectResponse> listarOfertasParaFiltro(Integer entidadeId);
}
