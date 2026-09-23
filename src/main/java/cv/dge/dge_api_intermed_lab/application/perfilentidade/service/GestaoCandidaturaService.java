package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaAvaliacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaAgendamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EntrevistaResultadoRequest;
import java.util.List;

public interface GestaoCandidaturaService {

    List<CandidaturaListaResponse> listar(CandidaturaFiltro filtro);

    CandidaturaDetalheResponse buscarPorId(Integer id, Integer entidadeId);

    CandidaturaDetalheResponse avaliar(Integer id, Integer entidadeId, CandidaturaAvaliacaoRequest request);

    EntrevistaResponse agendarEntrevista(
            Integer candidaturaId,
            Integer entidadeId,
            EntrevistaAgendamentoRequest request
    );

    List<EntrevistaResponse> listarEntrevistas(Integer candidaturaId, Integer entidadeId);

    EntrevistaResponse registarResultadoEntrevista(
            Integer candidaturaId,
            Integer entrevistaId,
            Integer entidadeId,
            EntrevistaResultadoRequest request
    );
}
