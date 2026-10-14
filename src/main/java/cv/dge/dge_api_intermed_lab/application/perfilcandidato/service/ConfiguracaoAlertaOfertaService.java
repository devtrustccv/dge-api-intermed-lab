package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.alerta.AlertaOfertaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.alerta.AlertaOfertaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.alerta.AlertaOfertaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.alerta.AlertaOfertaRequest;
import java.util.List;

public interface ConfiguracaoAlertaOfertaService {

    List<AlertaOfertaListaResponse> listar(Long pessoaId, String search);

    default List<AlertaOfertaListaResponse> listar(Long pessoaId) {
        return listar(pessoaId, null);
    }

    AlertaOfertaOpcoesResponse listarOpcoes(Long pessoaId, String ilha);

    AlertaOfertaDetalheResponse buscarPorId(Integer alertaId, Long pessoaId);

    AlertaOfertaDetalheResponse criar(Long pessoaId, AlertaOfertaRequest request);

    AlertaOfertaDetalheResponse atualizar(Integer alertaId, Long pessoaId, AlertaOfertaRequest request);
}
