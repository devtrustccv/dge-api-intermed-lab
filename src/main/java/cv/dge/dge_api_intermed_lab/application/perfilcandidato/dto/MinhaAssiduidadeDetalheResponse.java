package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MinhaAssiduidadeDetalheResponse(
        Integer assiduidadeId,
        String tipoAssiduidade,
        String tipoAssiduidadeDescricao,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime horaSaida,
        String estado,
        String estadoDescricao,
        String justificacao,
        String comprovativoPath,
        String comprovativoUrl,
        String observacao
) {
}
