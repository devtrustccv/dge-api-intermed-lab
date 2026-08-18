package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record VagaFiltro(
        String tipoOferta,
        Integer entidadeId,
        String ilha,
        String concelho,
        String estado,
        String codigoReferencia,
        Integer orientadorId,
        Integer coordenadorId,
        LocalDate dataInicio,
        LocalDate dataFim,
        String pesquisa
) {
}
