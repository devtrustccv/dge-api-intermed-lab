package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.relatorio;

public record RelatorioAcompanhamentoVinculo(
        Integer ofertaId,
        String codigoReferencia,
        Integer colocacaoId,
        Integer entidadeId,
        String denominacaoEntidade,
        Long pessoaId,
        String estagiario
) {
}
