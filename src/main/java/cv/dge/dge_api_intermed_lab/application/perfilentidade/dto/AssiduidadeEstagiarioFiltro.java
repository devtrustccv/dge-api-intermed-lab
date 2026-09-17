package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AssiduidadeEstagiarioFiltro(
        Integer entidadeId,
        Long estagiarioId,
        Integer ofertaId,
        String tipoAssiduidade,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime horaSaida,
        String estado
) {
}
