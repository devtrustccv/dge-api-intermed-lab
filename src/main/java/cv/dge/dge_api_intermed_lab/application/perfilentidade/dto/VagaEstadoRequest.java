package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record VagaEstadoRequest(
        String novoEstado,
        String observacao,
        String utilizador
) {
}
