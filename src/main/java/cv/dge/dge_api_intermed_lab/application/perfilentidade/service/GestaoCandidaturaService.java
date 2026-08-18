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

    CandidaturaDetalheResponse buscarPorId(Integer id);

    CandidaturaDetalheResponse avaliar(Integer id, CandidaturaAvaliacaoRequest request);

    EntrevistaResponse agendarEntrevista(Integer candidaturaId, EntrevistaAgendamentoRequest request);

    List<EntrevistaResponse> listarEntrevistas(Integer candidaturaId);

    EntrevistaResponse registarResultadoEntrevista(
            Integer candidaturaId,
            Integer entrevistaId,
            EntrevistaResultadoRequest request
    );
}
