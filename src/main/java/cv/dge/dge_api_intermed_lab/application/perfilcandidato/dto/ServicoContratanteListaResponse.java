package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServicoContratanteListaResponse(
        Integer servicoId,
        String tipoServico,
        String titulo,
        String estado,
        String estadoDescricao,
        LocalDate inicioCandidatura,
        LocalDate fimCandidatura,
        LocalDateTime dataRegisto
) {
}
