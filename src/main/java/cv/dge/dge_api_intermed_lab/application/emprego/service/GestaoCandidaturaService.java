package cv.dge.dge_api_intermed_lab.application.emprego.service;

import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaAvaliacaoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EntrevistaAgendamentoRequest;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.emprego.dto.EntrevistaResultadoRequest;
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
