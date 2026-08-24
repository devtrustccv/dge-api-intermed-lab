package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.util.List;

public record ConsultaVagasResponse(
        Long totalOfertas,
        Long totalEmprego,
        Long totalEstagio,
        List<ConsultaVagaListaResponse> ofertas,
        List<ConsultaVagaMapaResponse> pontosMapa
) {
}
