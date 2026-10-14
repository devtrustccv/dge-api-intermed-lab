package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.candidatura;

import java.time.LocalDate;

public record CandidaturaFiltro(
        Integer entidadeId,
        Long candidatoId,
        String candidato,
        String estado,
        String tipoOferta,
        Integer ofertaId,
        String tituloOferta,
        String canal,
        LocalDate dataInicio,
        LocalDate dataFim,
        String search
) {
    public CandidaturaFiltro(
            Integer entidadeId, Long candidatoId, String candidato, String estado,
            String tipoOferta, Integer ofertaId, String tituloOferta, String canal,
            LocalDate dataInicio, LocalDate dataFim
    ) {
        this(entidadeId, candidatoId, candidato, estado, tipoOferta, ofertaId,
                tituloOferta, canal, dataInicio, dataFim, null);
    }
}
