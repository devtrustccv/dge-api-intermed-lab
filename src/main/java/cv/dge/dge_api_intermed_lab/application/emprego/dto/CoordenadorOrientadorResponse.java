package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDateTime;

public record CoordenadorOrientadorResponse(
        Integer id,
        String tipo,
        String tipoDesc,
        String nome,
        Long pessoaId,
        String cargo,
        String email,
        String telemovel,
        String estado,
        String estadoDesc,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
