package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita;

import java.util.List;

public record VisitaTecnicaAvaliacaoItemRequest(
        List<VisitaTecnicaCandidatoRequest> candidatos,
        String criterio,
        String avaliacao,
        String observacao
) {
}
