package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.vaga;

public record VagaColaboradorSelectResponse(
        Integer id,
        String tipo,
        String denominacao,
        String email,
        String telefone
) {
}
