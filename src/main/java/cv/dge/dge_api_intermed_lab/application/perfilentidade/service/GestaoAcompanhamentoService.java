package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.acompanhamento.AcompanhamentoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.acompanhamento.AcompanhamentoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.acompanhamento.AcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.acompanhamento.AcompanhamentoOfertaSelectResponse;
import java.util.List;

public interface GestaoAcompanhamentoService {

    List<AcompanhamentoEstagiarioListaResponse> listarEstagiariosSelecionados(
            AcompanhamentoEstagiarioFiltro filtro
    );

    List<AcompanhamentoEstagiarioSelectResponse> listarEstagiariosSelecionadosParaFiltro(Integer entidadeId);

    List<AcompanhamentoOfertaSelectResponse> listarOfertasComEstagiariosSelecionados(Integer entidadeId);
}
