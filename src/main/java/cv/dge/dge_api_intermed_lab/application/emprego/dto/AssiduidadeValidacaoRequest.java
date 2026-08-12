package cv.dge.dge_api_intermed_lab.application.emprego.dto;

public record AssiduidadeValidacaoRequest(
        String decisao,
        String motivoIndeferimento,
        String observacao,
        String utilizador
) {
}
