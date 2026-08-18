package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record CoordenadorOrientadorRequest(
        String numeroDocumento,
        String nome,
        String tipo,
        String cargo,
        String email,
        String telemovel,
        String utilizador
) {
}
