package cv.dge.dge_api_intermed_lab.application.perfilentidade.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EntrevistaResponse(
        Integer id,
        Integer candidaturaId,
        Long pessoaId,
        String nomeCandidato,
        LocalDate dataEntrevista,
        LocalTime horario,
        String canal,
        String canalDesc,
        String localEntrevista,
        String parecer,
        String parecerDesc,
        String observacao,
        String estado,
        String estadoDesc,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
