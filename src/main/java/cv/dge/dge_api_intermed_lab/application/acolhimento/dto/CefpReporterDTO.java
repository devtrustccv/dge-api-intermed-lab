package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

public record CefpReporterDTO(
        Integer id,
        Integer organizationId,
        String denominacao,
        String endereco,
        String telefone,
        String email
) {
}
