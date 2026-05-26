package cv.dge.dge_api_intermed_lab.application.orientacao.service;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoRequest;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface OrientacaoServicoService {

    OrientacaoServicoResponse guardar(OrientacaoServicoRequest request);

    OrientacaoServicoResponse guardar(OrientacaoServicoRequest request, List<MultipartFile> ficheiros);

    OrientacaoServicoResponse buscarPorEntrevista(Integer idEntrevista);

    OrientacaoServicoResponse buscarPorEntrevistaETipoServico(Integer idEntrevista, String tipoServico);
}
