package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServicoCandidatoListaResponse(
        Integer servicoId,
        Integer candidaturaId,
        String tipoServico,
        String requerente,
        String estado,
        String estadoDescricao,
        LocalDate inicioCandidatura,
        LocalDate fimCandidatura,
        String selecaoIefp,
        String selecaoIefpDescricao,
        String statusAceitacao,
        String statusAceitacaoDescricao,
        LocalDateTime dataRegisto
) {
}
