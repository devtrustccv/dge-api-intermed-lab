package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaOpcoesResponse;
import java.util.List;

public interface MinhaCandidaturaService {

    List<MinhaCandidaturaListaResponse> listar(MinhaCandidaturaFiltro filtro);

    MinhaCandidaturaDetalheResponse buscarPorId(Integer candidaturaId, Long pessoaId);

    MinhaCandidaturaOpcoesResponse listarOpcoes(Long pessoaId, String ilha);
}
