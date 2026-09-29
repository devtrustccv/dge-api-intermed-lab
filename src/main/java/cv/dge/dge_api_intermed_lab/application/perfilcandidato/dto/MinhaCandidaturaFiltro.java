package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;

public record MinhaCandidaturaFiltro(
        Long pessoaId,
        String tipoOferta,
        Integer entidadeId,
        String entidade,
        String ilha,
        String concelho,
        String estado,
        String codigoReferencia,
        LocalDate dataInicio,
        LocalDate dataFim
) {
    public MinhaCandidaturaFiltro(
            Long pessoaId,
            String tipoOferta,
            Integer entidadeId,
            String ilha,
            String concelho,
            String estado,
            String codigoReferencia,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        this(
                pessoaId,
                tipoOferta,
                entidadeId,
                null,
                ilha,
                concelho,
                estado,
                codigoReferencia,
                dataInicio,
                dataFim
        );
    }
}
