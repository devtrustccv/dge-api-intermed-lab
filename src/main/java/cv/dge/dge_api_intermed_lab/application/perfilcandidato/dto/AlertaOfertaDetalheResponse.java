package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.time.LocalDateTime;

public record AlertaOfertaDetalheResponse(
        Integer alertaId,
        Long pessoaId,
        String pessoaNome,
        String tipoOferta,
        String tipoOfertaDescricao,
        String ilha,
        String ilhaDescricao,
        String concelho,
        String concelhoDescricao,
        Integer entidadeId,
        String entidadeDescricao,
        String habilitacaoLiteraria,
        String habilitacaoLiterariaDescricao,
        String nivelQualificacao,
        String nivelQualificacaoDescricao,
        String estado,
        String estadoDescricao,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
