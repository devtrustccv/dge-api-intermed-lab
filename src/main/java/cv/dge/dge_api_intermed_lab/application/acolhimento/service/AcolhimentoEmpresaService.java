package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEmpresaRequest;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoEmpresaResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AcolhimentoEmpresaService {

    AcolhimentoEmpresaResponse registar(AcolhimentoEmpresaRequest request);

    AcolhimentoEmpresaResponse registar(AcolhimentoEmpresaRequest request, List<MultipartFile> ficheiros);
}
