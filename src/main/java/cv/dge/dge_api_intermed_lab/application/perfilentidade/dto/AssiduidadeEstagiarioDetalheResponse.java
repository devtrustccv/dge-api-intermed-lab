package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AssiduidadeEstagiarioDetalheResponse(
        Integer id,
        Integer colocacaoId,
        Integer ofertaId,
        String oferta,
        Integer entidadeId,
        String denominacaoEntidade,
        Long estagiarioId,
        String estagiario,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime horaSaida,
        String tipoAssiduidade,
        String tipoAssiduidadeDesc,
        String justificacao,
        String estado,
        String estadoDesc,
        String observacao,
        String comprovativo,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
