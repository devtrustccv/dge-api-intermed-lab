package cv.dge.dge_api_intermed_lab.application.acolhimento.service;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoRequest;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoReporterResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AcolhimentoService {

    AcolhimentoReporterResponse buscarParaReporter(Integer idAcolhimento);

    AcolhimentoRegistoResponse registar(AcolhimentoRegistoRequest request);

    AcolhimentoRegistoResponse registar(AcolhimentoRegistoRequest request, List<MultipartFile> ficheiros);
}
