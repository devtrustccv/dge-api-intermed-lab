package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.candidatura;

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
        LocalDate dataFim,
        String search
) {
    public MinhaCandidaturaFiltro(
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
        this(
                pessoaId, tipoOferta, entidadeId, entidade, ilha, concelho,
                estado, codigoReferencia, dataInicio, dataFim, null
        );
    }

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
                dataFim,
                null
        );
    }
}
