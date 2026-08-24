package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;

public record ConsultaVagaFiltro(
        Long pessoaId,
        String tipoOferta,
        Integer entidadeId,
        String ilha,
        String concelho,
        String estado,
        String codigoReferencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        String pesquisa
) {
}
