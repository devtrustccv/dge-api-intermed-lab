package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MinhaAssiduidadeRequest(
        String tipoAssiduidade,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime horaSaida,
        String justificacao,
        String utilizador
) {
}
