package cv.dge.dge_api_intermed_lab.application.emprego.dto;

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
        Integer coordenadorId,
        String coordenadorDenominacao,
        String coordenadorEmail,
        String coordenadorTelefone,
        String codigoReferencia,
        String estado,
        String estadoDesc,
        LocalDate dataFimCandidatura
) {
}
