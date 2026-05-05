package cv.dge.dge_api_intermed_lab.web.orientacao;

import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.service.OrientacaoEntrevistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orientacao/entrevistas")
public class OrientacaoEntrevistaController {

    private final OrientacaoEntrevistaService orientacaoEntrevistaService;

    @GetMapping("{id}")
    public OrientacaoEntrevistaResponse buscarPorId(@PathVariable Integer id) {
        return orientacaoEntrevistaService.buscarPorId(id);
    }
}
