package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.util.List;

public record ConsultaVagaMapaResponse(
        String ilha,
        String ilhaDesc,
        String concelho,
        String concelhoDesc,
        Long totalOfertas,
        Long totalAbertas,
        Long totalATerminar,
        Long totalEncerradas,
        String situacao,
        String situacaoDesc,
        List<Integer> ofertaIds
) {
}
