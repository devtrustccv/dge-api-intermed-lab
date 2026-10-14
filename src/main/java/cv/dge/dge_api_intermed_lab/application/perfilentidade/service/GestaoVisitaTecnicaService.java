package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaAtualizacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaCandidatoSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaCefpSelectResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaExecutadoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaObservacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaValidacaoRequest;
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
