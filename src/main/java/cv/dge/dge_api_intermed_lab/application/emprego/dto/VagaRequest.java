package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record VagaRequest(
        String codigoReferencia,
        String tipoOferta,
        String titulo,
        String descricao,
        LocalDate dataInicioCandidatura,
        LocalDate dataFimCandidatura,
        LocalDate dataInicioPrevisto,
        Integer duracaoContrato,
        String regimeContrato,
        Integer entidadeId,
        String denominacaoEntidade,
        String habilitacaoMinima,
        String nivelQualificacao,
        Integer numVagas,
        String habilitacaoMaxima,
        Object conhecimentoLinguistico,
        Object competenciasValorizadas,
        LocalTime horaInicio,
        LocalTime horaFim,
        Object diasSemana,
        Object cursosAreaFormacao,
        Object experienciaProfissional,
        String ilha,
        String concelho,
        Integer orientadorId,
        Integer coordenadorId,
        String emailContacto,
        String contacto,
        String observacao,
        String utilizador
) {
}
