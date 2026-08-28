package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;

public record VagaListaResponse(
        Integer id,
        String titulo,
        String tipoOferta,
        String tipoOfertaDesc,
        String ilha,
        String ilhaDesc,
        String concelho,
        String concelhoDesc,
        String localOferta,
        Integer numVagas,
        Integer entidadeId,
        String denominacaoEntidade,
        Integer orientadorId,
        String orientadorDenominacao,
        String orientadorNome,
        Integer coordenadorId,
        String coordenadorDenominacao,
        String coordenadorNome,
        String coordenadorEmail,
        String coordenadorTelefone,
        String codigoReferencia,
        String estado,
        String estadoDesc,
        LocalDate dataInicio,
        LocalDate dataFimCandidatura
) {
}