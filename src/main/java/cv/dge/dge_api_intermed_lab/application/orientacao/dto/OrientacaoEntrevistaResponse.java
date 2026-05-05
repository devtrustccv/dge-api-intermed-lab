package cv.dge.dge_api_intermed_lab.application.orientacao.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public record OrientacaoEntrevistaResponse(
        Integer id,
        Integer idAcolhimento,
        Integer idUtente,
        Integer idTecnico,
        String nomeTecnico,
        LocalDate dataEntrevista,
        LocalTime horaInicio,
        LocalTime horaFim,
        String local,
        String statusEntrevista,
        Integer idCefp,
        String cefp,
        String tipoServico,
        String canal,
        String localEntrevista,
        Map<String, Object> resultadoEntrevista,
        String parecerIo,
        String obsParecerIo,
        String pathResultado,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate,
        Map<String, Object> acolhimento,
        OrientacaoServicoResponse servico
) {
}
