package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.servico;

import java.time.LocalDate;

public record ServicoContratanteFiltro(
        Long pessoaId,
        String tipoServico,
        String estado,
        LocalDate dataInicio,
        LocalDate dataFim,
        String search
) {
    public ServicoContratanteFiltro(
            Long pessoaId,
            String tipoServico,
            String estado,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        this(pessoaId, tipoServico, estado, dataInicio, dataFim, null);
    }
}
