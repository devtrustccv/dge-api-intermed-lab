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

    VisitaTecnicaDetalheResponse buscarPorId(Integer id, Integer entidadeId);

    VisitaTecnicaDetalheResponse criar(Integer entidadeId, VisitaTecnicaRequest request);

    VisitaTecnicaDetalheResponse atualizar(Integer id, Integer entidadeId, VisitaTecnicaAtualizacaoRequest request);

    VisitaTecnicaDetalheResponse validar(Integer id, Integer entidadeId, VisitaTecnicaValidacaoRequest request);

    VisitaTecnicaDetalheResponse marcarComoExecutado(
            Integer id,
            Integer entidadeId,
            VisitaTecnicaExecutadoRequest request
    );

    VisitaTecnicaDetalheResponse registarObservacoes(
            Integer id,
            Integer entidadeId,
            VisitaTecnicaObservacaoRequest request
    );

    List<VisitaTecnicaCandidatoSelectResponse> listarCandidatos(Integer entidadeId);

    List<VisitaTecnicaCefpSelectResponse> listarCefps(Integer entidadeId);
}
