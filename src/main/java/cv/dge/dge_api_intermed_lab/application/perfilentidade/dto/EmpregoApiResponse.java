package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.util.List;

public record EmpregoApiResponse<T>(
        boolean sucesso,
        String mensagem,
        T dados,
        List<String> erros
) {

    public static <T> EmpregoApiResponse<T> sucesso(String mensagem, T dados) {
        return new EmpregoApiResponse<>(true, mensagem, dados, List.of());
    }

    public static EmpregoApiResponse<Void> erro(String mensagem, List<String> erros) {
        return new EmpregoApiResponse<>(false, mensagem, null, erros == null ? List.of() : erros);
    }
}
