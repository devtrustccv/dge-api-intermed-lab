package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDateTime;

public record CandidaturaListaResponse(
        Integer id,
        Long pessoaId,
        String nomeCandidato,
        String tipoOferta,
        String tipoOfertaDesc,
        Integer ofertaId,
        String tituloOferta,
        String canal,
        String canalDesc,
        String statusCandidatura,
        String statusCandidaturaDesc,
        Boolean selecaoIefp,
        Boolean podeAvaliar,
        Boolean podeAgendarEntrevista,
        LocalDateTime dataCandidatura
) {
}
