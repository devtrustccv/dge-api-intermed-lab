package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoDominioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.EmpregoDominioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dominios")
public class EmpregoDominioController {

    private final EmpregoDominioService dominioService;

    @GetMapping
    public EmpregoApiResponse<List<EmpregoDominioResponse>> consultar(
            @RequestParam("dominio") String dominio
    ) {
        List<EmpregoDominioResponse> itens = dominioService.consultarTodosCampos(dominio);
        return EmpregoApiResponse.sucesso(
                "Domínio \"" + dominio.trim() + "\" carregado do IGRP com sucesso.",
                itens
        );
    }
}
