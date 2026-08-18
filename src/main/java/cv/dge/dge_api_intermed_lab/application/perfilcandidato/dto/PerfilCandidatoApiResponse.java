package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.util.List;

public record PerfilCandidatoApiResponse<T>(
        boolean sucesso,
        String mensagem,
        T dados,
        List<String> erros
) {

    public static <T> PerfilCandidatoApiResponse<T> sucesso(String mensagem, T dados) {
        return new PerfilCandidatoApiResponse<>(true, mensagem, dados, List.of());
    }

    public static PerfilCandidatoApiResponse<Void> erro(String mensagem, List<String> erros) {
        return new PerfilCandidatoApiResponse<>(false, mensagem, null, erros == null ? List.of() : erros);
    }
}
