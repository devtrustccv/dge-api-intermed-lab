package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio.RelatorioAcompanhamentoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio.RelatorioAcompanhamentoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio.RelatorioAcompanhamentoOfertaSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio.RelatorioAcompanhamentoRemoverRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio.RelatorioAcompanhamentoRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface GestaoRelatorioAcompanhamentoService {
    List<RelatorioAcompanhamentoListaResponse> listar(RelatorioAcompanhamentoFiltro filtro);
    RelatorioAcompanhamentoDetalheResponse buscarPorId(Integer id, Integer entidadeId);
    RelatorioAcompanhamentoDetalheResponse criar(Integer entidadeId, RelatorioAcompanhamentoRequest request);
    RelatorioAcompanhamentoDetalheResponse criar(
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request,
            MultipartFile relatorioAnexo
    );
    RelatorioAcompanhamentoDetalheResponse atualizar(Integer id, Integer entidadeId, RelatorioAcompanhamentoRequest request);
    RelatorioAcompanhamentoDetalheResponse atualizar(
            Integer id,
            Integer entidadeId,
            RelatorioAcompanhamentoRequest request,
            MultipartFile relatorioAnexo
    );
    RelatorioAcompanhamentoDetalheResponse remover(Integer id, Integer entidadeId, RelatorioAcompanhamentoRemoverRequest request);
    List<RelatorioAcompanhamentoOfertaSelectResponse> listarOpcoes(Integer entidadeId);
}
