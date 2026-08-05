package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;

public record CoordenadorOrientadorFiltro(
        String nome,
        String tipo,
        String estado,
        LocalDate dataInicio,
        LocalDate dataFim
) {
}
