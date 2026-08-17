package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.RelatorioAcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.RelatorioAcompanhamentoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.RelatorioAcompanhamentoRequest;
import java.util.List;

public interface GestaoRelatorioAcompanhamentoService {
    List<RelatorioAcompanhamentoListaResponse> listar(RelatorioAcompanhamentoFiltro filtro);
    RelatorioAcompanhamentoDetalheResponse buscarPorId(Integer id, Integer entidadeId);
    RelatorioAcompanhamentoDetalheResponse criar(Integer entidadeId, RelatorioAcompanhamentoRequest request);
    RelatorioAcompanhamentoDetalheResponse atualizar(Integer id, Integer entidadeId, RelatorioAcompanhamentoRequest request);
    RelatorioAcompanhamentoDetalheResponse remover(Integer id, Integer entidadeId, RelatorioAcompanhamentoRemoverRequest request);
    List<RelatorioAcompanhamentoEstagiarioSelectResponse> listarEstagiarios(Integer entidadeId);
}
