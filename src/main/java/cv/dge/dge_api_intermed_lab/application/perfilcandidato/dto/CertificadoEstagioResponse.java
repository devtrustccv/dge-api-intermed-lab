package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record CertificadoEstagioResponse(
        Integer certificadoId,
        Integer colocacaoId,
        Long pessoaId,
        Integer candidaturaId,
        String nome,
        String naturalidade,
        LocalDate dataNascimento,
        String numeroDocumento,
        String habilitacaoAcademica,
        String nomeEntidade,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal classificacaoFinal,
        String assinatura,
        LocalDateTime dataEmissao,
        String codigoContraprova,
        boolean emitido,
        Map<String, String> substituicoes
) {
}

