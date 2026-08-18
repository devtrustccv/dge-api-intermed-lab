package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record PessoaGlobalResponse(
        Long id,
        String nome,
        String email,
        String telemovel,
        String tipoDocumento,
        String numeroDocumento
) {
}
