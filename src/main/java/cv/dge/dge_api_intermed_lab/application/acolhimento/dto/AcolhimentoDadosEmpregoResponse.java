package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import java.time.LocalDateTime;

public record AcolhimentoDadosEmpregoResponse(
        Integer id,
        Integer idPessoa,
        Integer idUtente,
        String situacaoEmprego,
        String profissao,
        String empresa,
        String setorAtividade,
        String ilha,
        String concelho,
        String zona,
        String telefone,
        String numTrabalhador,
        String duracao,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
