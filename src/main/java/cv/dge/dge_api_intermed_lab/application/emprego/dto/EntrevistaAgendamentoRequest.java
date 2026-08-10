package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EntrevistaAgendamentoRequest(
        LocalDate dataEntrevista,
        LocalTime horario,
        String canal,
        String localEntrevista,
        String utilizador
) {
}
