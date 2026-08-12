package cv.dge.dge_api_intermed_lab.web.pac;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.acolhimento.dto.PacCandidaturaResponse;
import cv.dge.dge_api_intermed_lab.application.acolhimento.service.AcolhimentoConsultaService;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class PacController {

    private static final Pattern CAMPO_TOKEN_PATTERN = Pattern.compile("([^.\\[\\]]+)|\\[(\\d+|[^\\]]+)\\]");

    private final AcolhimentoConsultaService acolhimentoConsultaService;
    private final ObjectMapper objectMapper;

// enpoint para pac lista de utentes
    @GetMapping("pac-candidatura/contratacao")
    public List<PacCandidaturaResponse> listarUtentes(
            @RequestParam(required = false) Integer cefpId,
            @RequestParam(required = false) String denominacao
    ) {
        return acolhimentoConsultaService.listarCandidaturas(cefpId, denominacao);
    }

}
