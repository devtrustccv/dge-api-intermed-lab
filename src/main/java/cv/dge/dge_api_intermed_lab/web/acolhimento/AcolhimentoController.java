package cv.dge.dge_api_intermed_lab.web.acolhimento;

import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoRequest;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoRegistoResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.AcolhimentoReporterResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.service.AcolhimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/acolhimentos")
public class AcolhimentoController {

    private final AcolhimentoService acolhimentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AcolhimentoRegistoResponse registar(@RequestBody AcolhimentoRegistoRequest request) {
        return acolhimentoService.registar(request);
    }

    @GetMapping("reporter/{id}")
    public AcolhimentoReporterResponse buscarParaReporter(@PathVariable("id") Integer idAcolhimento) {
        return acolhimentoService.buscarParaReporter(idAcolhimento);
    }
}
