package cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.dashboard;

public record EvolucaoCandidaturaMensalResponse(
        Integer mes,
        String mesDescricao,
        Long totalEmprego,
        Long totalEstagio
) {
}
