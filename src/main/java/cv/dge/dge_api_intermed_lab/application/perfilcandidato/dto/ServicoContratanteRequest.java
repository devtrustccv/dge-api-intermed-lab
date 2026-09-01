package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ServicoContratanteRequest(
        String tipoServico,
        String titulo,
        String descricao,
        LocalDate dataPretendida,
        BigDecimal valorPrevisto,
        String competenciasExigidas,
        LocalDate inicioCandidatura,
        LocalDate fimCandidatura,
        String ilha,
        String concelho,
        String zona,
        String telefone,
        String email,
        List<String> anexosMantidos,
        String utilizador
) {
}
