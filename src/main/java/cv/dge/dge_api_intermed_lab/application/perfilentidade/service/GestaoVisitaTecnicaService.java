package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaAtualizacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaCefpSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaExecutadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaObservacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaValidacaoRequest;
import java.util.List;

public interface GestaoVisitaTecnicaService {

    List<VisitaTecnicaListaResponse> listar(VisitaTecnicaFiltro filtro);

    VisitaTecnicaDetalheResponse buscarPorId(Integer id);

    VisitaTecnicaDetalheResponse criar(VisitaTecnicaRequest request);

    VisitaTecnicaDetalheResponse atualizar(Integer id, VisitaTecnicaAtualizacaoRequest request);

    VisitaTecnicaDetalheResponse validar(Integer id, VisitaTecnicaValidacaoRequest request);

    VisitaTecnicaDetalheResponse marcarComoExecutado(Integer id, VisitaTecnicaExecutadoRequest request);

    VisitaTecnicaDetalheResponse registarObservacoes(Integer id, VisitaTecnicaObservacaoRequest request);

    List<VisitaTecnicaCandidatoSelectResponse> listarCandidatos(Integer entidadeId);

    List<VisitaTecnicaCefpSelectResponse> listarCefps();
}
