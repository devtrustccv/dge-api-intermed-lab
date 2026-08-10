package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;

public record ColocacaoCandidatoFiltro(
        String tipoOferta,
        String codigoReferencia,
        Long pessoaId,
        String tipoContrato,
        LocalDate dataInicioPrevisto,
        LocalDate dataRegistoInicio,
        LocalDate dataRegistoFim,
        Integer entidadeId
) {
}
