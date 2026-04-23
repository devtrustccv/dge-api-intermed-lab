package cv.dge.dge_api_intermed_lab.application.acolhimento.dto;

public record EntidadeReporterDTO(
        Integer id,
        Integer globalIdEntidade,
        String denominacao,
        Integer nif,
        String registoSocial,
        String naturezaJuridica
) {
}
