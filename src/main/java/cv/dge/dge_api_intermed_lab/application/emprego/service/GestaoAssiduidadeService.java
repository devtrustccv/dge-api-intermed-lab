package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AssiduidadeValidacaoRequest;
import java.util.List;

public interface GestaoAssiduidadeService {

    List<AssiduidadeEstagiarioListaResponse> listar(AssiduidadeEstagiarioFiltro filtro);

    AssiduidadeEstagiarioDetalheResponse buscarPorId(Integer id, Integer entidadeId);

    AssiduidadeEstagiarioDetalheResponse validar(Integer id, Integer entidadeId, AssiduidadeValidacaoRequest request);

    List<AssiduidadeEstagiarioSelectResponse> listarEstagiariosParaFiltro(Integer entidadeId);

    List<AssiduidadeOfertaSelectResponse> listarOfertasParaFiltro(Integer entidadeId);
}
