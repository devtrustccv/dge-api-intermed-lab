package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.vaga;

import java.time.LocalDate;

public record ConsultaVagaFiltro(
        String tipoOferta,
        Integer entidadeId,
        String entidade,
        String ilha,
        String concelho,
        String estado,
        String codigoReferencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        String search,
        String situacao
) {
    public ConsultaVagaFiltro(
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
        this(
                tipoOferta,
                entidadeId,
                entidade,
                ilha,
                concelho,
                estado,
                codigoReferencia,
                dataInicio,
                dataFim,
                search,
                null
        );
    }
}
