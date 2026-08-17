package cv.dge.dge_api_intermed_lab.application.emprego.dto;

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
