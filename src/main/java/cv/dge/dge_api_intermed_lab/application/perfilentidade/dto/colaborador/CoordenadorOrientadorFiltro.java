package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colaborador;

import java.time.LocalDate;

public record CoordenadorOrientadorFiltro(
        Integer entidadeId,
        String nome,
        String tipo,
        String estado,
        LocalDate dataInicio,
        LocalDate dataFim,
        String search
) {
    public CoordenadorOrientadorFiltro(
            Integer entidadeId, String nome, String tipo, String estado,
            LocalDate dataInicio, LocalDate dataFim
    ) {
        this(entidadeId, nome, tipo, estado, dataInicio, dataFim, null);
    }
}
