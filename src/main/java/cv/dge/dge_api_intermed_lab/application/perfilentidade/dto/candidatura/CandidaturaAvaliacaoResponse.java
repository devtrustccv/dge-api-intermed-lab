package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.candidatura;

public record CandidaturaAvaliacaoResponse(
        String parecer,
        String parecerDesc,
        String motivoRecusa,
        Boolean selecaoIefp,
        Boolean podeAvaliar
) {
}
