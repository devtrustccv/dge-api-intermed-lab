package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade;

import java.time.LocalDate;
import java.time.LocalTime;

public record AssiduidadeEstagiarioFiltro(
        Integer entidadeId,
        Long estagiarioId,
        String estagiario,
        Integer ofertaId,
        String oferta,
        String tipoAssiduidade,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime horaSaida,
        String estado,
        String search
) {
    public AssiduidadeEstagiarioFiltro(
            Integer entidadeId, Long estagiarioId, String estagiario, Integer ofertaId,
            String oferta, String tipoAssiduidade, LocalDate data, LocalTime horaEntrada,
            LocalTime horaSaida, String estado
    ) {
        this(entidadeId, estagiarioId, estagiario, ofertaId, oferta, tipoAssiduidade,
                data, horaEntrada, horaSaida, estado, null);
    }
}
