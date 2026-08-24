package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDateTime;

public record AdesaoJovemResponse(
        Integer adesaoId,
        Long pessoaId,
        Integer utenteId,
        String situacaoProfissional,
        LocalDateTime dataRegisto,
        String utilizadorRegisto
) {
}
