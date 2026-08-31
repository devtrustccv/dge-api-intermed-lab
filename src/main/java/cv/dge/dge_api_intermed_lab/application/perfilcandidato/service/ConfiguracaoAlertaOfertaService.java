package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaRequest;
import java.util.List;

public interface ConfiguracaoAlertaOfertaService {

    List<AlertaOfertaListaResponse> listar(Long pessoaId);

    AlertaOfertaOpcoesResponse listarOpcoes(Long pessoaId, String ilha);

    AlertaOfertaDetalheResponse buscarPorId(Integer alertaId, Long pessoaId);

    AlertaOfertaDetalheResponse criar(Long pessoaId, AlertaOfertaRequest request);

    AlertaOfertaDetalheResponse atualizar(Integer alertaId, Long pessoaId, AlertaOfertaRequest request);
}
