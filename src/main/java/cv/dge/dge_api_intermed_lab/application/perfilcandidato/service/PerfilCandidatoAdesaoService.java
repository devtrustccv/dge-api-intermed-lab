package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AdesaoJovemResponse;

public interface PerfilCandidatoAdesaoService {

    AdesaoJovemResponse carregarFormulario(Long pessoaId);

    AdesaoJovemResponse confirmarAdesao(Long pessoaId, AdesaoJovemRequest request);
}
