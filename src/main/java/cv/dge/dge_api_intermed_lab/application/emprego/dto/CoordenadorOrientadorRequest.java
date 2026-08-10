package cv.dge.dge_api_intermed_lab.application.emprego.dto;

public record CoordenadorOrientadorRequest(
        Long pessoaId,
        String nome,
        String tipo,
        String cargo,
        String email,
        String telemovel,
        String utilizador
) {
}
