package cv.dge.dge_api_intermed_lab.application.orientacao.dto;

import java.time.LocalDateTime;

public record RequisitoResponse(
        Integer id,
        String requisito,
        String tipoServico,
        LocalDateTime dateCreate,
        String userCreate,
        String estado,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
