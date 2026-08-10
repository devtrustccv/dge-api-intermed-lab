package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ColocacaoCandidatoListaResponse(
        Integer id,
        String tipoOferta,
        String tipoOfertaDesc,
        String codigoReferencia,
        Long pessoaId,
        String nomeCandidato,
        String tipoContrato,
        String tipoContratoDesc,
        LocalDate dataInicioPrevisto,
        LocalDateTime dataRegisto
) {
}
