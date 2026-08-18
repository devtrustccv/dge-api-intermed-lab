package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoOfertaSelectResponse;
import java.util.List;

public interface GestaoAcompanhamentoService {

    List<AcompanhamentoEstagiarioListaResponse> listarEstagiariosSelecionados(
            AcompanhamentoEstagiarioFiltro filtro
    );

    List<AcompanhamentoEstagiarioSelectResponse> listarEstagiariosSelecionadosParaFiltro();

    List<AcompanhamentoOfertaSelectResponse> listarOfertasComEstagiariosSelecionados();
}
