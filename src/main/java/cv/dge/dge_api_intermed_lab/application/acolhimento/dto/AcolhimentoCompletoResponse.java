package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoEntrevistaResponse;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
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
        OrientacaoEntrevistaResponse entrevista,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        OrientacaoServicoResponse servico,
        List<DocumentoResponseDTO> documentos,
        Map<String, Object> detalhes
) {
}
