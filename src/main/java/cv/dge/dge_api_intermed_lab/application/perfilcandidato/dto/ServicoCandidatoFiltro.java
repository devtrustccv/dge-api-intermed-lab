package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;

public record ServicoCandidatoFiltro(
        Long pessoaId,
        String tipoServico,
        String estado,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
