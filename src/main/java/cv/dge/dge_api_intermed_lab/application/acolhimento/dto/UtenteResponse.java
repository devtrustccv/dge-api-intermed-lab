package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UtenteResponse(
        Integer id,
        Integer pessoaId,
        String nome,
        LocalDate dataNascimento,
        String tipoDocumento,
        String numDocumento,
        String sexo,
        Integer nif,
        String habilitacaoLiteraria,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
