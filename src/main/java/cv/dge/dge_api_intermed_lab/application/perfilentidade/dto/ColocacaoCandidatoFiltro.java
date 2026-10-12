package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record ColocacaoCandidatoFiltro(
        String tipoOferta,
        String codigoReferencia,
        Long pessoaId,
        String candidato,
        String tipoContrato,
        LocalDate dataInicioPrevisto,
        LocalDate dataRegistoInicio,
        LocalDate dataRegistoFim,
        Integer entidadeId,
        String pesquisa
) {
    public ColocacaoCandidatoFiltro(
            String tipoOferta, String codigoReferencia, Long pessoaId, String candidato,
            String tipoContrato, LocalDate dataInicioPrevisto, LocalDate dataRegistoInicio,
            LocalDate dataRegistoFim, Integer entidadeId
    ) {
        this(tipoOferta, codigoReferencia, pessoaId, candidato, tipoContrato,
                dataInicioPrevisto, dataRegistoInicio, dataRegistoFim, entidadeId, null);
    }
}
