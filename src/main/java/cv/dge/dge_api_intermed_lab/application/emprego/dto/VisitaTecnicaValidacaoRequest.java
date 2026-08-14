package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDateTime;

public record VisitaTecnicaValidacaoRequest(
        String parecer,
        LocalDateTime novaData,
        String motivoIndeferimento,
        String utilizador
) {
}
