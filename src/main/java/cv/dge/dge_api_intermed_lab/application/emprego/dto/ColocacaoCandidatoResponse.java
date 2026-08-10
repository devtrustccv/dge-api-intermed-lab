package cv.dge.dge_api_intermed_lab.application.emprego.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ColocacaoCandidatoResponse(
        Integer id,
        Integer idOferta,
        String tipoOferta,
        String tipoOfertaDesc,
        String codigoReferencia,
        Integer entidadeId,
        String denominacaoEntidade,
        Long pessoaId,
        String nomeCandidato,
        Integer idCandidatura,
        String tipoContrato,
        String tipoContratoDesc,
        Integer duracaoContrato,
        LocalDate dataInicioPrevisto,
        LocalDate dataFimPrevisto,
        String descricao,
        String contratoPath,
        String estado,
        String estadoDesc,
        Boolean registadoCefp,
        LocalDateTime dateCreate,
        String userCreate,
        LocalDateTime dateUpdate,
        String userUpdate
) {
}
