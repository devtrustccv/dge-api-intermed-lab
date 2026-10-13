package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.adesao.AdesaoJovemRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.adesao.AdesaoJovemResponse;

public interface PerfilCandidatoAdesaoService {

    AdesaoJovemResponse confirmarAdesao(Long pessoaId, AdesaoJovemRequest request);
}
