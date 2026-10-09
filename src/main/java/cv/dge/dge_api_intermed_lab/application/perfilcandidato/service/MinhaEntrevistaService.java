package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaEntrevistaListaResponse;
import java.util.List;

public interface MinhaEntrevistaService {

    List<MinhaEntrevistaListaResponse> listar(Long pessoaId);
}
