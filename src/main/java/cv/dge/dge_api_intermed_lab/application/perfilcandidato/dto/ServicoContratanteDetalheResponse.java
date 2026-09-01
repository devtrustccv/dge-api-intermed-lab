package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ServicoContratanteDetalheResponse(
        Integer servicoId,
        Long contratanteId,
        String nomeContratante,
        String tipoServico,
        String titulo,
        String descricao,
        LocalDate dataPretendida,
        BigDecimal valorPrevisto,
        String competenciasExigidas,
        LocalDate inicioCandidatura,
        LocalDate fimCandidatura,
        String ilha,
        String ilhaDescricao,
        String concelho,
        String concelhoDescricao,
        String zona,
        String zonaDescricao,
        String telefone,
        String email,
        List<ServicoContratanteAnexoResponse> anexos,
        String estado,
        String estadoDescricao,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
