package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AssiduidadeEstagiarioListaResponse(
        Integer id,
        Integer colocacaoId,
        Integer ofertaId,
        String oferta,
        Long estagiarioId,
        String estagiario,
        String tipoAssiduidade,
        String tipoAssiduidadeDesc,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime horaSaida,
        String horario,
        String estado,
        String estadoDesc,
        Boolean podeValidar
) {
}
