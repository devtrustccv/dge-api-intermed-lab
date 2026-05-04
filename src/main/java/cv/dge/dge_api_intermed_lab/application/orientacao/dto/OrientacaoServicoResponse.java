package cv.dge.dge_api_intermed_lab.application.orientacao.dto;

import java.util.Map;

public record OrientacaoServicoResponse(
        Integer id,
        Integer idEntrevista,
        Integer idAcolhimento,
        Integer idUtente,
        String tipoUtente,
        String tipoUtenteDesc,
        String tipoServico,
        String tipoServicoDesc,
        Boolean necessidadeAnalise,
        Map<String, Object> detalhesServico,
        Map<String, Object> detalhesAnalise
) {
}
