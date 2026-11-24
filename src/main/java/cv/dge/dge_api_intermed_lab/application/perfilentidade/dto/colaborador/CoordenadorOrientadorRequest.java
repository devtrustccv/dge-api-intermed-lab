package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador;

public record CoordenadorOrientadorRequest(
        String tipoDocumento,
        String numeroDocumento,
        Long pessoaId,
        String nome,
        String tipo,
        String cargo,
        String email,
        String telemovel,
        String utilizador
) {
}
