package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;

public record VisitaTecnicaValidacaoRequest(
        String parecer,
        LocalDate novaData,
        String motivoIndeferimento,
        String utilizador
) {
}
