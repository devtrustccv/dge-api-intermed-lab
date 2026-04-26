package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AcolhimentoCompletoResponse(
        Integer idAcolhimento,
        Integer idPessoa,
        Integer idUtente,
        Integer idEntidade,
        String denominacaoUtente,
        BigDecimal nif,
        Integer cefpId,
        Integer orgId,
        String tipoUtente,
        String tipoUtenteDesc,
        String tipoServico,
        String tipoServicoDesc,
        String canal,
        String canalDesc,
        String fonteInformacao,
        String statusEntrevista,
        String numInscricao,
        Integer idTecnicoAtendimento,
        String tecnicoAtendimento,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate,
        CefpReporterDTO cefp,
        UtenteReporterDTO utente,
        EntidadeReporterDTO entidade,
        AcolhimentoDadosEmpregoResponse dadosEmprego,
        List<DocumentoResponseDTO> documentos,
        Map<String, Object> detalhes
) {
}
