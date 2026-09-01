package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoCandidatoOpcoesResponse;
import java.util.List;

public interface ServicoCandidatoService {

    List<ServicoCandidatoListaResponse> listar(ServicoCandidatoFiltro filtro);

    ServicoCandidatoOpcoesResponse listarOpcoes();

    ServicoCandidatoDetalheResponse buscarPorId(Integer servicoId, Long pessoaId);

    ServicoCandidatoDetalheResponse aceitar(Integer servicoId, Long pessoaId, String utilizador);

    ServicoCandidatoDetalheResponse recusar(Integer servicoId, Long pessoaId, String utilizador);
}
