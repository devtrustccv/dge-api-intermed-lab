package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;

public record MinhaAssiduidadeFiltro(
        Long pessoaId,
        String tipoAssiduidade,
        String estado,
        LocalDate dataInicio,
        LocalDate dataFim,
        String pesquisa
) {
    public MinhaAssiduidadeFiltro(
            Long pessoaId,
            String tipoAssiduidade,
            String estado,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        this(pessoaId, tipoAssiduidade, estado, dataInicio, dataFim, null);
    }
}
