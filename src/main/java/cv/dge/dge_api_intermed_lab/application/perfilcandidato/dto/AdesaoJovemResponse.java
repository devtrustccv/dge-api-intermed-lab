package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdesaoJovemResponse(
        Integer adesaoId,
        Long pessoaId,
        String foto,
        String nif,
        String nacionalidade,
        LocalDate dataNascimento,
        String sexo,
        String tipoDocumentoIdentificacao,
        String numeroDocumentoIdentificacao,
        String localEmissao,
        LocalDate dataValidade,
        String estadoCivil,
        String situacaoProfissional,
        Boolean adesaoRegistada,
        Boolean podeSubmeter,
        LocalDateTime dataAdesao,
        String utilizadorRegisto
) {
}
