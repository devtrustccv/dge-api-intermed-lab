package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RelatorioAcompanhamentoDetalheResponse(
        Integer id,
        Integer ofertaId,
        String codigoReferencia,
        Integer colocacaoId,
        Integer entidadeId,
        String denominacaoEntidade,
        Long pessoaId,
        String estagiario,
        LocalDate dataInicio,
        LocalDate dataFim,
        String atividadesRealizadas,
        String dificuldades,
        String recomendacoes,
        String relatorioAnexo,
        String estado,
        String estadoDescricao,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
