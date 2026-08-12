package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoEstagiarioSelectResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.AcompanhamentoOfertaSelectResponse;
import java.util.List;

public interface GestaoAcompanhamentoService {

    List<AcompanhamentoEstagiarioListaResponse> listarEstagiariosSelecionados(
            AcompanhamentoEstagiarioFiltro filtro
    );

    List<AcompanhamentoEstagiarioSelectResponse> listarEstagiariosSelecionadosParaFiltro();

    List<AcompanhamentoOfertaSelectResponse> listarOfertasComEstagiariosSelecionados();
}
