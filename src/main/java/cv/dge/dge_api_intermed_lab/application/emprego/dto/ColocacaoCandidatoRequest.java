package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;

public record ColocacaoCandidatoRequest(
        String tipoOferta,
        Integer ofertaId,
        String codigoReferencia,
        Long pessoaId,
        String tipoContrato,
        Integer duracaoContrato,
        LocalDate dataInicioPrevisto,
        LocalDate dataFimPrevisto,
        String descricao,
        String contratoPath,
        String utilizador
) {
}
