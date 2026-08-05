package cv.dge.dge_api_intermed_lab.application.emprego.dto;

public record CoordenadorOrientadorRequest(
        String tipoDocumento,
        String numeroDocumento,
        Integer pessoaId,
        String nome,
        String tipo,
        String cargo,
        String email,
        String telemovel,
        String utilizador
) {
}
