package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CandidaturaListaResponse(
        Integer id,
        Long pessoaId,
        String nomeCandidato,
        LocalDate dataNascCandidato,
        String sexoCandidato,
        String emailCandidato,
        String telefoneCandidato,
        String ilhaConcelhoCandidato,
        String moradaCandidato,
        String habilitacaoLiterariaCandidato,
        String tipoOferta,
        String tipoOfertaDesc,
        Integer ofertaId,
        String codigoOferta,
        String tituloOferta,
        String canal,
        String canalDesc,
        String tipoDocumento,
        Object anexo,
        String statusCandidatura,
        String statusCandidaturaDesc,
        String motivoRecusa,
        Boolean selecaoIefp,
        Boolean podeAvaliar,
        Boolean podeAgendarEntrevista,
        Integer entrevistaId,
        Boolean podeRegistarResultadoEntrevista,
        LocalDateTime dataCandidatura
) {
}
