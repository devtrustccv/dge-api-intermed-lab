package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDateTime;

public record CandidaturaDetalheResponse(
        Integer id,
        String tipoOferta,
        String tipoOfertaDesc,
        Integer ofertaId,
        String codigoOferta,
        String tituloOferta,
        Integer entidadeId,
        String denominacaoEntidade,
        LocalDateTime dataCandidatura,
        CandidatoDetalheResponse candidato,
        Object anexos,
        String statusCandidatura,
        String statusCandidaturaDesc,
        String motivoRecusa,
        Boolean selecaoIefp,
        Boolean podeAvaliar,
        Boolean podeAgendarEntrevista,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
