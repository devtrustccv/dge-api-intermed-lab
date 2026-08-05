package cv.dge.dge_api_intermed_lab.application.emprego.dto;

public record PessoaGlobalResponse(
        Integer id,
        String nome,
        String email,
        String telemovel,
        String tipoDocumento,
        String numeroDocumento
) {
}
