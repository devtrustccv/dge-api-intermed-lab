package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;

public record CandidatoDetalheResponse(
        Long pessoaId,
        String nome,
        LocalDate dataNascimento,
        String sexo,
        String email,
        String telemovel,
        String ilhaId,
        String ilhaDesc,
        String concelhoId,
        String concelhoDesc,
        String localidadeId,
        String localidadeDesc,
        String localizacao,
        String morada,
        String habilitacaoAcademica
) {
}
