package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDateTime;

public record CoordenadorOrientadorListaResponse(
        Integer id,
        String tipo,
        String tipoDesc,
        String nome,
        String email,
        String telemovel,
        String estado,
        String estadoDesc,
        LocalDateTime dateCreate,
        String userCreate
) {
}
