package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;

public record ServicoContratanteCandidatoListaResponse(
        Integer candidaturaId,
        Long pessoaId,
        String nomeCandidato,
        String tipoServico,
        String titulo,
        String estado,
        String estadoDescricao,
        String selecaoIefp,
        String selecaoIefpDescricao,
        LocalDate dataCandidatura
) {
}
