package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRequest;
import java.util.List;

public interface GestaoRelatorioAcompanhamentoService {
    List<RelatorioAcompanhamentoListaResponse> listar(RelatorioAcompanhamentoFiltro filtro);
    RelatorioAcompanhamentoDetalheResponse buscarPorId(Integer id, Integer entidadeId);
    RelatorioAcompanhamentoDetalheResponse criar(Integer entidadeId, RelatorioAcompanhamentoRequest request);
    RelatorioAcompanhamentoDetalheResponse atualizar(Integer id, Integer entidadeId, RelatorioAcompanhamentoRequest request);
    RelatorioAcompanhamentoDetalheResponse remover(Integer id, Integer entidadeId, RelatorioAcompanhamentoRemoverRequest request);
    List<RelatorioAcompanhamentoOfertaSelectResponse> listarOpcoes(Integer entidadeId);
}
