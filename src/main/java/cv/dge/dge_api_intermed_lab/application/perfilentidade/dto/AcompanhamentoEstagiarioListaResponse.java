package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

public record AcompanhamentoEstagiarioListaResponse(
        Integer candidaturaId,
        Long estagiarioId,
        String estagiario,
        Integer ofertaId,
        String oferta,
        Integer entrevistaId,
        String parecerEntrevista,
        String parecerEntrevistaDesc,
        String estadoEntrevista,
        String estadoEntrevistaDesc,
        Integer colocacaoId,
        Boolean podeVerAvaliacao
) {
}
